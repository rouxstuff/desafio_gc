# 04 · Metodologia do saneamento (IA & Dados)

Este documento resume como a base foi analisada e limpa, e deixa claro o que foi feito automaticamente e o que ficou para validação humana.

[← Voltar ao README](../README.md)

> **Nota sobre nomes:** a base original chama os compradores de "fornecedor". Neste documento, "comprador" e "fornecedor" são a mesma coluna da planilha.

## 1. Ponto de partida: diagnóstico da base

| Indicador | Valor |
|---|---|
| Linhas (código × comprador) | 9.795 |
| Códigos de serviço distintos | 7.937 |
| Compradores | 111 |
| Linhas 100% idênticas / valores nulos | 0 / 0 |

Cada linha é a relação entre um código e um comprador, por isso linhas e códigos são números diferentes. A base está **completa, mas despadronizada**: não há nulos nem duplicatas exatas, e mesmo assim 546 descrições se repetem em códigos diferentes depois da normalização.

**Principais achados**

- **Repetição:** 1.234 códigos aparecem em mais de uma linha (esperado: mesmo código, compradores diferentes). O problema real é a **mesma descrição em códigos diferentes**: 375 só ajustando maiúsculas e espaços, 546 (1.250 códigos) após normalizar siglas e prefixos.
- **Padrão por grupo:** em Civil e Eletromecânica a repetição ocorre sobretudo **entre compradores**; em Facilities pesa mais a repetição **dentro do mesmo comprador** (315 linhas), o que indica código duplicado por quem cadastrou.
- **Limite de 40 caracteres:** 1.519 descrições têm exatamente 40 caracteres. Na amostra revisada, cerca de metade está cortada no meio de palavra; estimativa ajustada de ≈ 730 cortadas de fato (faixa de 550 a 930).
- **Prefixos de sistema:** só 57% das descrições não têm prefixo; as demais usam `SV `, `SV-` ou `H-`.
- **Unidade de medida:** dos ≈ 3.900 itens de mão de obra, 3.123 puderam ser verificados e 706 (22,6%) estão com unidade diferente da esperada. O desvio mais forte é em itens mensais: 201 de 207 estão em SV em vez de MON.

> As marcações do diagnóstico são aproximadas por palavras-chave e foram conferidas por amostra, por uma pessoa. Os percentuais ajustados devem ser lidos com as faixas informadas.

## 2. O que foi feito automaticamente (risco baixo)

Só foram unidos códigos cujo **nome fica idêntico depois da padronização textual** (maiúsculas, acentos, pontuação, prefixos `SV`/`H-` e siglas como HE, HN, NOT). Como o texto é o mesmo, o risco de erro é baixo.

Também foram unidas, e sinalizadas para conferência (20 itens `JUNCAO_POR_PREFIXO`), descrições cortadas no limite de 40 caracteres que correspondiam a um único serviço da mesma categoria, e um código que aparecia com duas descrições (`FAC-4048071`).

Resultado da planilha saneada: **7.937 → 7.323 códigos** (−614; −7,7%) e **nenhum comprador perdido**.

**Limpeza posterior (já no sistema):** 8 serviços `ALMOCO PREMIUM` duplicados (séries 03 e 04, categoria Insumos) foram removidos, mantendo a série 02. Com isso o catálogo do sistema fica em **7.315 códigos** (−622; −7,8%). O script é `desafiogc/db/02-limpar-almoco-duplicado.sql` (também em `desafiogc/docs/limpar-almoco-duplicado.sql`) e o De-Para (`depara_almoco_removidos.csv`) acompanha a entrega, fora do repositório. O script move vínculos e contratações dos códigos removidos para os mantidos, é idempotente e renomeia "ALMOCO PREMIUM - 02 ..." para "ALMOCO PREMIUM ...". A planilha `Gerdau_saneamento_atualizado.xlsx` **não foi atualizada** para essa limpeza.

Ação aplicada a cada linha da base (aba *Base unificada*):

| Ação | Linhas | Significado |
|---|---|---|
| `PREFIXO_ADICIONADO` | 8.757 | Só ganhou o prefixo da categoria |
| `PREFIXO_ADICIONADO_NOME_PADRONIZADO` | 400 | Prefixo e nome padronizado |
| `UNIFICADO` | 638 | Código novo diferente do original (unido a outro igual) |

## 3. O que foi deixado para decisão humana

### 3.1 Pares parecidos, mas não idênticos

Foram comparados serviços **dentro de cada grupo** com semelhança de caracteres (TF-IDF) e de palavras (mínimo 82/100). Foram **descartados 8.968 pares** que diferem em modificador (HE, HN, NOT, DF, SS...), número, tamanho, sinal +/- ou "C/" × "S/", porque são serviços diferentes. Sobraram **974 pares**:

| Nível | Pares | O que são | Códigos a menos se todos aprovados |
|---|---|---|---|
| **ALTA** | 99 | Erro de digitação, abreviação ou plural (TEC × TECNICO, CAPINEIRO × CAPINEIROS) | 99 |
| **MÉDIA** | 409 | Um nome é o outro com palavras a mais, ou palavras próximas | 376 |
| **BAIXA** | 466 | Palavras diferentes (cargo, material, local); provavelmente não são duplicados | 389 |

Teto teórico: 7.315 → 6.451 serviços (−11,8%). A faixa BAIXA mistura serviços diferentes, então o número real tende a ser menor. (Os pares foram calculados antes da limpeza do almoço.)

### 3.2 Por que não unificamos por semelhança automaticamente

1. **Nomes quase iguais podem ser serviços diferentes.** Exemplos reais: `TRECHO TRILHO 90M C/LINHA DE VIDA` × `S/LINHA DE VIDA`; `VIGILANTE ARMADO 24H` × `DESARMADO 24H`; `RETIRADA FORRO PVC C/APROVEITAMENTO` × `S/APROVEITAMENTO`. Um programa de texto vê 95% de semelhança; o contrato vê serviços e preços diferentes.
2. **Unir códigos afeta contratos.** O código absorvido deixa de existir e os contratos que o usam precisam migrar.
3. **A base não tem como desempatar.** São só texto, grupo e unidade: não há preço, especificação técnica nem histórico de contrato.
4. **Nomes de 40 caracteres podem estar cortados**, e dois nomes parecidos podem ser o mesmo texto cortado em pontos diferentes.
5. **Errar uma união custa mais do que não fazer.** Um par deixado separado pode ser unido depois; uma união indevida precisa ser desfeita nos contratos.

### 3.3 Revisão manual (aba *Revisão manual* do catálogo)

3.044 itens que precisam de confirmação da Gerdau. Nada foi corrigido automaticamente.

| Tipo | Itens | O que é |
|---|---|---|
| `POSSIVEL_NOME_CORTADO` | 1.899 | Descrição com 40 caracteres, possivelmente cortada |
| `UNIDADE_SUGERIDA` | 856 | Há uma unidade provavelmente mais correta que a cadastrada |
| `NOME_CORTADO_AMBIGUO` | 240 | Nome cortado que pode corresponder a mais de um serviço (marcado com sufixo `#NNNN`) |
| `UNIDADE_DIVERGENTE` | 28 | Compradores usam unidades diferentes para o mesmo serviço |
| `JUNCAO_POR_PREFIXO` | 20 | Descrição cortada unida a um serviço da mesma categoria com prefixo único |
| `CODIGO_COM_DUAS_DESCRICOES` | 1 | O mesmo código aparece com descrições diferentes |

## 4. Três níveis de similaridade (hipótese da Entrega 2)

A Entrega 2 propôs um funil em três níveis. Ele continua válido como método; o que foi **aplicado** é o nível 1 e o que foi **entregue para decisão** vem dos níveis 2 e 3.

| Nível | Critério | Tratamento |
|---|---|---|
| 1 | Descrição idêntica após padronização, mesma categoria e unidade | Consolidação determinística (aplicada) |
| 2 | Mão de obra: mesma função, mesma categoria de custo e mesma condição (ex.: `AJUDANTE - HORA EXTRA À DISPOSIÇÃO` = `SV AJUDANTE HE DISP`) | Assinatura estruturada |
| 3 | Itens que não são mão de obra: TF-IDF híbrido (palavras de 1 a 2 gramas e caracteres de 3 a 5 gramas) com limite de **0,72**, comparando só dentro do mesmo grupo e unidade. Grupos **fortes** têm todos os pares ≥ **0,85** | Hipótese a validar |

**Travas:** exigem os mesmos números, medidas e numerais romanos. Sem elas, 245 de 504 grupos (49%) misturavam medidas diferentes, como `TUBO RIGIDO SOLDAVEL 25MM` com `50MM`. Por isso `AJUDANTE HE 50` e `AJUDANTE HE 100` (0,84 de similaridade textual) **não** são unidos.

> **Para não confundir:** os limites 0,72 e 0,85 são da **análise de dados** (agrupar a base). O **filtro do sistema** usa outros limites, pensados para a entrada de novos códigos: 0,60 para sugerir e 0,90 para bloquear (veja [Fluxo de governança](05-fluxo-de-governanca.md)).

## 5. O que acontece quando a Gerdau decidir

A planilha `pares_semelhantes_para_revisao.xlsx` tem uma coluna **Decisão** (`UNIFICAR` ou `MANTER SEPARADO`; em branco = nada muda). Ao unificar, mantém-se por padrão o código com mais compradores (coluna *Sugestão de código a manter*), e a Gerdau pode indicar outro em *Código a manter*. Pares com alerta **UNIDADE DIFERENTE** pedem confirmação da unidade do código mantido. Decisões parciais já são aplicáveis.

O processo previsto lê a planilha preenchida e gera: o catálogo sem os códigos unificados, os vínculos apontando para o código mantido (sem repetição) e um **De-Para** (código antigo → código mantido) para migrar contratos. Foi o mesmo desenho usado, em pequeno, na limpeza do `ALMOCO PREMIUM`.

> O script `aplicar_decisoes.py` citado no PDF "Unificação por semelhança" **não faz parte deste repositório** e não foi verificado pela equipe do sistema. Os números de teste do PDF (99 pares da faixa ALTA: 7.323 → 7.224 códigos) devem ser lidos como resultado daquele script.

Até a resposta da Gerdau, o sistema funciona com o catálogo atual e o filtro de entrada já impede novas duplicatas.
