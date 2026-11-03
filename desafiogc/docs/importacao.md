# Como importar o catálogo saneado

O sistema já está pronto para receber o catálogo limpo por CSV. Quem faz isso é o administrador, em **Importação** (menu).

## Antes de começar
1. Faça backup do banco: `pg_dump -Fc -U <usuario> <banco> > backup_antes_da_importacao.dump`
2. Dados de demonstração: por padrão o sistema **não** cria fornecedores, serviços e vínculos fictícios
   (`APP_SEED_DEMO=false`). Se o seu banco ainda tem os dados fictícios do desenvolvimento, apague-os antes
   (ordem: `fornecedor_servico`, `servico`, `fornecedor`) ou use um banco novo.
3. Categorias existentes: CIVIL, ELETROMECÂNICA e FACILITIES (prefixos CIV, ELETRO e FAC). Uma categoria nova
   é criada pelo próprio CSV, preenchendo a coluna `prefixo_categoria` (2 a 10 letras, único).

## Código com prefixo da categoria
Todo serviço fica com o código **PREFIXO-CÓDIGO** (ex.: `CIV-4057137`, `ELETRO-4033776`, `FAC-4048071`).
O código que já existe (SAP) é mantido **sem alteração**: o sistema só acrescenta o prefixo da categoria na importação.
- Pode enviar o CSV com `4057137` (o sistema vira `CIV-4057137`) ou já com `CIV-4057137` (não duplica o prefixo).
- No CSV de **vínculos** vale o mesmo: `4057137` ou `CIV-4057137`.
- Códigos criados pelo sistema **continuam do último número da categoria** (se o maior é `FAC-4048071`, o próximo é `FAC-4048072`) e **nunca repetem** um código que já existe. Categoria sem nenhum código numerado começa em `PREFIXO-000001`.

## Limpar o banco antes de reimportar (se já importou sem prefixo)
Os códigos antigos (sem prefixo) conflitam por nome com os novos, então limpe antes:
```sql
truncate table fornecedor_servico, servico, fornecedor restart identity;
update categoria set ultimo_numero = 0;
```
(`categoria` e `unidade_medida` ficam como estão.)

## Passo a passo
1. **Serviços** → envie o CSV de serviços com **"Só simular"** marcado. Corrija o que o relatório apontar
   (ele mostra o número da linha) e simule de novo até zerar os erros.
2. Desmarque "Só simular" e envie. É tudo ou nada: se houver erro, nada é gravado.
3. **Vínculos** → mesmo processo (primeiro simular). Só serviços APROVADOS podem ser vinculados.
4. Confira no banco (modelo abaixo) e abra o Catálogo.

## O que o sistema valida
- código vazio ou com mais de 20 caracteres; nome vazio ou acima de 40 caracteres (limite do SAP);
- categoria inexistente sem `prefixo_categoria`; unidade de medida inexistente;
- o mesmo código repetido com dados diferentes;
- **o mesmo nome na mesma categoria com códigos diferentes** (é duplicado: resolva no saneamento).
- Pode reenviar o mesmo arquivo: códigos que já existem são pulados, nada é duplicado nem sobrescrito.

## Formato dos arquivos
Veja `docs/modelos/`. Separador `;` ou `,`; UTF-8 ou CSV do Excel. Os cabeçalhos da planilha original
(Grupo de Serviço, Código do Fornecedor, Código do Serviço, Descrição do Item, Unidade medida) também funcionam.

## Conferência rápida (SQL)
```sql
select status, count(*) from servico group by status;
select c.categoria_nome, c.prefixo, count(*) from servico s join categoria c on c.id = s.categoria_id group by 1, 2;
select count(*) from fornecedor;
select count(*) from fornecedor_servico;
```

## Dicionário de abreviações do filtro
`src/main/resources/normalizacao/regras.csv` (HE = hora extra, PERIC = periculosidade etc.). É um ponto de partida:
acrescente as regras do saneamento e use **Recarregar índice** (ou reinicie). Não precisa mexer em código.
