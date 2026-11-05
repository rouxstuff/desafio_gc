# Alterações de performance (problema N+1 com Supabase)

Causa: a aba Fornecedores fazia ~9 mil consultas (uma por vínculo) e cada uma é uma ida e volta de rede ao Supabase.

| Onde | Antes | Depois |
|---|---|---|
| `CatalogoPaginaController.montarFornecedores()` | por fornecedor, carregava todos os serviços dele (lista que a tela nem usava): ~9.000 queries | só id e rótulo: 1 query |
| `/vinculos` – listas suspensas | carregava 7.317 entidades `Servico` com joins | projeção leve `ServicoOpcaoView` (código + nome): 1 query |
| `/vinculos?servicoFiltro=` | carregava cada vínculo + fornecedor + serviço (EAGER) | `listarFornecedoresDoServico`: 1 query |
| `GET /fornecedor/{id}/servicos` | 1 query por vínculo | `listarServicosDoFornecedor` com `join fetch`: 1 query |
| `FornecedorServico` | `@ManyToOne` EAGER (causa do N+1) | `fetch = LAZY` |
| Importação de vínculos | ~9.400 INSERTs individuais (IDENTITY desliga o batch do Hibernate) | INSERTs em lotes JDBC de 500 |

Dica extra: na `DB_URL` do Supabase acrescente `?reWriteBatchedInserts=true` para acelerar ainda mais os lotes.

Como conferir: com `show-sql: true`, abra a aba Fornecedores e conte as queries no log (esperado: poucas, não milhares).

# Prefixo da categoria nos códigos
- `CodigoComPrefixo` (novo) + `ImportacaoService`: a importação acrescenta o prefixo da categoria ao código existente
  (4057137 -> CIV-4057137) sem alterá-lo; vínculos aceitam o código com ou sem prefixo.
- O gerador continua do último número da categoria (maior número entre os códigos PREFIXO-NÚMERO existentes e o contador): FAC-4048071 -> próximo FAC-4048072.
- `ServicoService.gerarCodigo` pula números que já existem, então nunca repete um código.

# Quantidade e observação ao contratar / solicitar
- Serviço APROVADO com unidade de medida: a tela de detalhes mostra a unidade (somente leitura), **quantidade** (obrigatória)
  e **observação**. Unidade de medida e adicionais não são oferecidos (e o servidor ignora se vierem no POST).
- Demais casos (aprovado sem unidade, pendente, rejeitado): continua escolhendo unidade e adicionais; quantidade é opcional
  e só vale com unidade escolhida.
- Solicitar novo serviço: quantidade (opcional, validada pela unidade escolhida) e observação.
- Regras (`QuantidadeServico`): ST, ZCJ e SV só inteiro; demais unidades decimal com vírgula (até 3 casas); sempre > 0;
  ponto não é aceito (evita "1.500" virar 1,5). Observação até 200 caracteres.
- Limite atual: a confirmação só exibe a quantidade na tela de conclusão; ainda não grava a contratação no banco.
