-- Remove os ALMOCO PREMIUM duplicados (séries 03 e 04) e mantém um serviço por faixa.
-- Cada faixa (11 A 20, 21 A 40, 41 A 60, ACIMA DE 60) fica com um único serviço, e o nome perde o "- 02":
-- ALMOCO PREMIUM (11 A 20), (21 A 40), (41 A 60) e (ACIMA DE 60).
-- Os códigos dos serviços mantidos NÃO mudam (são códigos do SAP; contratos e vínculos dependem deles). Pode rodar mais de uma vez: se já foi aplicado, não faz nada.
--
-- Docker, banco já criado:   docker compose exec -T db psql -U gerdau -d desafiogc < db/02-limpar-almoco-duplicado.sql
-- Docker, primeira subida:   este arquivo roda sozinho depois do restore.

BEGIN;

CREATE TEMP TABLE depara (antigo text, mantido text) ON COMMIT DROP;
INSERT INTO depara (antigo, mantido) VALUES
    ('INS-4102915', 'INS-4102911'),
    ('INS-4102916', 'INS-4102912'),
    ('INS-4102917', 'INS-4102913'),
    ('INS-4102918', 'INS-4102914'),
    ('INS-4102919', 'INS-4102911'),
    ('INS-4103230', 'INS-4102912'),
    ('INS-4103231', 'INS-4102913'),
    ('INS-4103232', 'INS-4102914');

-- 1) Quem estava vinculado ao serviço removido passa a ficar vinculado ao mantido (sem repetir).
INSERT INTO fornecedor_servico (fornecedor_id, codigo_servico)
SELECT DISTINCT fs.fornecedor_id, d.mantido
FROM fornecedor_servico fs
JOIN depara d ON d.antigo = fs.codigo_servico
WHERE EXISTS (SELECT 1 FROM servico s WHERE s.codigo_servico = d.mantido)
  AND NOT EXISTS (SELECT 1 FROM fornecedor_servico x
                  WHERE x.fornecedor_id = fs.fornecedor_id AND x.codigo_servico = d.mantido);

DELETE FROM fornecedor_servico WHERE codigo_servico IN (SELECT antigo FROM depara);

-- 2) Contratações já registradas (a tabela só existe depois que a aplicação subiu uma vez).
DO $$
BEGIN
    IF to_regclass('public.contratacao') IS NOT NULL THEN
        UPDATE contratacao c
           SET codigo_servico = d.mantido,
               nome_servico = (SELECT s.nome_servico FROM servico s WHERE s.codigo_servico = d.mantido)
          FROM depara d
         WHERE c.codigo_servico = d.antigo
           AND EXISTS (SELECT 1 FROM servico s WHERE s.codigo_servico = d.mantido);
    END IF;
END $$;

-- 3) Remove os serviços duplicados.
DELETE FROM servico WHERE codigo_servico IN (SELECT antigo FROM depara);

-- 4) Tira o "- 02" do nome dos que ficaram.
UPDATE servico SET nome_servico = replace(nome_servico, 'ALMOCO PREMIUM - 02 ', 'ALMOCO PREMIUM ')
 WHERE codigo_servico IN ('INS-4102911','INS-4102912','INS-4102913','INS-4102914')
   AND nome_servico LIKE 'ALMOCO PREMIUM - 02 %';

DO $$
BEGIN
    IF to_regclass('public.contratacao') IS NOT NULL THEN
        UPDATE contratacao SET nome_servico = replace(nome_servico, 'ALMOCO PREMIUM - 02 ', 'ALMOCO PREMIUM ')
         WHERE codigo_servico IN ('INS-4102911','INS-4102912','INS-4102913','INS-4102914');
    END IF;
END $$;

COMMIT;

-- Conferência (deve mostrar 4 linhas):
-- SELECT codigo_servico, nome_servico FROM servico WHERE nome_servico LIKE 'ALMOCO PREMIUM%' ORDER BY 1;
