# 03 · Regra de nome e padronização

[← Voltar ao README](../README.md)

## 1. Formato do código

`PREFIXO-CÓDIGO`, em que o prefixo identifica a categoria.

| Categoria | Prefixo | Exemplo |
|---|---|---|
| Civil | `CIV` | `CIV-4057137` |
| Eletromecânica | `ELETRO` | `ELETRO-4033776` |
| Facilities | `FAC` | `FAC-4048071` |
| Materiais | `MAT` | `MAT-4050910` |
| Insumos | `INS` | `INS-4103522` |

- O **código SAP original é mantido sem alteração**; o saneamento só acrescenta o prefixo. Quem já conhece `4057137` reconhece `CIV-4057137`.
- Limite do campo: **20 caracteres** (no catálogo atual o maior tem 14).
- **Códigos novos** criados pelo sistema seguem do último número da categoria e nunca repetem um existente. Ex.: se o maior código de Facilities é `FAC-4048071`, o próximo é `FAC-4048072`. Em categoria sem código numerado, começa em `PREFIXO-000001`.
- O mesmo número pode existir em duas categorias (ex.: `CIV-4050910` e `MAT-4050910`). O prefixo os separa, e a coluna *Observação* do catálogo marca esses casos.

## 2. Regra do nome

1. **Até 40 caracteres** (limite do SAP). O sistema recusa mais que isso.
2. **MAIÚSCULAS, sem acento**, espaços simples.
3. **Só o cargo ou a função** em serviços de mão de obra. Condição de trabalho (hora extra, noturno, periculosidade...) vai em campo próprio, não no nome.
4. **Sem prefixos de sistema** (`SV `, `SV-`, `EV`, `H-`). Eles existiam em 43% das descrições, em três convenções diferentes.
5. **Sem pontuação solta** e sem espaço duplicado.
6. Medidas escritas coladas: `25MM`, não `25 MM`.
7. **O nome é único no catálogo inteiro**: o sistema não aceita o mesmo nome em duas categorias (a comparação ignora maiúsculas e acentos, e serviços reprovados não contam). Se o nome já existe em outra categoria, a mensagem mostra o código do serviço existente.

### Antes e depois

| Original (como estava no SAP) | Nome padronizado |
|---|---|
| `SV-EQUIPE TELHADO` | `EQUIPE TELHADO` |
| `SV OFICIAL MENSAL` | `OFICIAL MENSAL` |
| `Pavimentação a frio por kg` | `PAVIMENTACAO A FRIO POR KG` |
| `DIARIA RESGATISTA NIVEL 3 ATE 8H .` | `DIARIA RESGATISTA NIVEL 3 ATE 8H` |
| `SV-EQUIPE PINTURA` | `EQUIPE PINTURA` |

> **Observação sobre a base entregue:** a regra "nome único no catálogo inteiro" vale para o que o sistema aceita **daqui para frente**. O catálogo importado manteve o mesmo nome em categorias diferentes onde isso já existia (por exemplo, `ACRILICO 6MM` em Civil). Unificar esses casos é decisão da Gerdau (veja [Limitações](07-limitacoes-e-proximos-passos.md)).

## 3. Dicionário de siglas

O dicionário fica em `desafiogc/src/main/resources/normalizacao/regras.csv` e **não exige mexer em código**. Formato: `expressão_regular;substituição`, uma regra por linha, aplicadas em ordem.

| Escritas encontradas | Padrão |
|---|---|
| `HORA EXTRA`, `HORAS EXTRAS`, `H EXTRA`, `HEXTRA`, `HX` | **HE** |
| `HORA NORMAL`, `HORAS NORMAIS`, `H NORMAL` | **HN** |
| `NOTURNO`, `NOTURNA`, `NOTURN` | **NOT** |
| `DOMINGO E FERIADO`, `DOM E FER`, `DOM FER`, `DOM FERIADO` | **DF** |
| `SEGUNDA A SABADO`, `SEG A SAB`, `SEG SAB` | **SS** |
| `A DISPOSICAO`, `DISPOSICAO` | **DISP** |
| `PERICULOSA`, `PERICULOSO`, `PERICUL`, `PERIC`, `PRERICULOSIDADE` (erro de grafia) | **PERICULOSIDADE** |
| `INSALUBRE`, `INSALUB`, `INSAL` | **INSALUBRIDADE** |
| `DE`, `DA`, `DO`, `DAS`, `DOS` | removidas |

**Modificadores** são as palavras que, se forem diferentes, indicam serviços diferentes: `HE, HN, NOT, DF, SS, DISP, PERICULOSIDADE, INSALUBRIDADE, MAIOR, MENOR`. Dois nomes que diferem num modificador **nunca são bloqueados** como duplicados.

Para acrescentar uma sigla nova: inclua uma linha no arquivo e use **Importação → Recarregar índice** (ou reinicie o sistema).

> O arquivo atual é um **ponto de partida** montado com os exemplos da Entrega 2. O dicionário completo da trilha IA & Dados pode ser acrescentado linha a linha.

## 4. Como o texto é preparado para comparar

Antes de qualquer comparação, o sistema transforma o nome nesta ordem (isso serve só para comparar; o nome exibido não muda):

1. maiúsculas e remoção de acentos;
2. `>` vira `MAIOR` e `<` vira `MENOR` (serviços de ">3 dias" e "<3 dias" são diferentes);
3. remoção de `(A)`, `(O)` de gênero;
4. `50 %` vira `50%`; vírgula e ponto decimais padronizados;
5. `+`, `/` soltos e pontuação viram espaço;
6. dicionário de siglas (seção 3);
7. `25 MM` vira `25MM` (também CM, KM, M2, M3, KG, ML, POL, KV, KVA, KW, HP, CV, HZ);
8. remoção de prefixo de sistema (`SV`, `EV`, `SERV`);
9. espaços simples.

## 5. Unidade de medida

As 17 unidades oficiais estão no sistema:

`CM`, `CDM`, `ZCJ` (conjunto), `TAG` (dia), `H` (hora), `L`, `MON` (mês), `M`, `M3`, `M2`, `ML`, `MM`, `KG`, `KM`, `SV` (serviço), `TO` (tonelada), `ST` (unidade/peça).

O diagnóstico mostrou que a unidade cadastrada **não é confiável**: 22,6% dos itens de mão de obra verificáveis estão com unidade diferente da esperada (quase todos em SV quando deveriam ser MON ou H). O catálogo traz a **unidade mais usada entre os compradores** e sinaliza divergências para revisão (veja [Metodologia](04-metodologia-do-saneamento.md)). Na tela, **só o administrador altera a unidade** de um serviço.
