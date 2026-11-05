<p align='center'>
    <img src='./assets/logo-gerdau.png' width='150'/>
</p>

# DESAFIO GC 2026 - GRUPO 16

**Geração Caldeira · Squad King Space Skills (KSS) · Trilhas Java e IA & Dados**

* Eduardo de Melo Scheibler (Scrum Master)
* Fernanda Cunha do Amaral
* Gabriela Martins Oliveira
* Kaio Alexsander Inácio da Silva Penha (Product Owner)
* Rafael Antonio Silva Castro da Silva

### DESAFIO PARA O GRUPO
Como organizar, simplificar e manter organizados os códigos de serviço da empresa, de forma que:
* Fique fácil descobrir se um código já existe antes de criar um novo (evitando duplicidade).
* Cada código tenha um nome padronizado e fácil de entender, geralmente esse nome é composto pelo “cargo” (mecânico, soldador, etc.) + “tipo de modificador” de custo em cima da hora normal (hora extra, adicional noturno, adicional de periculosidade etc.).
* Existam regras claras de como nomear e organizar novos códigos no futuro, tanto para mão de obra quanto custos indiretos.
* Exista um processo simples (fluxo) que qualquer pessoa possa seguir para pedir a criação de um novo código, com aprovação e controle.

---

# Nossa solução: Saneamento, Padronização e Filtro de Códigos de Serviço — Gerdau

## 1. O problema

Os contratos de serviço da Gerdau usam códigos para identificar cada serviço contratado. Hoje cada comprador cria os próprios códigos, sem filtro nem conferência. Com o tempo isso gerou milhares de códigos repetidos, escritos de formas diferentes e limitados a 40 caracteres no SAP, o que corta muitos nomes no meio.

## 2. O que entregamos

| # | Entrega | Onde está |
|---|---|---|
| 1 | **Catálogo saneado e padronizado** de serviços, com código novo no formato `PREFIXO-CÓDIGO` | planilha `Gerdau_saneamento_atualizado.xlsx` |
| 2 | **Lista de pares parecidos para a Gerdau decidir** (unificar ou manter separado) | planilha `pares_semelhantes_para_revisao.xlsx` e o PDF "Unificação por semelhança" |
| 3 | **Sistema web de entrada de novos códigos** (consulta, sugestão, bloqueio de duplicados e aprovação) | pasta `desafiogc/` (Java + Spring Boot) |
| 4 | **Documentação** para o time da Gerdau usar e repetir o processo | este material |

### Funcionalidades do sistema

- **Consulta** de serviços por nome, com tolerância a siglas e **pesquisa de vários termos de uma vez** (separados por `;`).
- **Sugestão e bloqueio** de duplicados na criação (≥ 60% sugere, ≥ 90% na mesma categoria bloqueia), com código gerado pelo sistema.
- **Aprovação** pelo administrador, com motivo obrigatório na reprovação. Serviço reprovado **só o administrador vê**.
- **Catálogo** paginado com busca por nome ou código (compradores e fornecedores).
- **Serviços** (só administrador): substitui o Catálogo para o admin, com a mesma lista e a opção **Alterar** (unidade de medida, unidade de custo e descrição). O administrador não contrata serviço.
- **Vínculos fornecedor × serviço** com barra de pesquisa por nome ou código junto da lista de serviços.
- **Importação** do catálogo saneado por CSV (tudo ou nada, com simulação).
- **Usuários**: login por **e-mail**; aba do administrador para criar usuários e ligar cada um a um fornecedor existente, com **um usuário por fornecedor**.

## 3. Resultados em números

| Indicador | Antes | Depois |
|---|---|---|
| Códigos de serviço distintos | 7.937 | **7.323** (−614, **−7,7%**) |
| Vínculos fornecedor × serviço | 9.795 linhas | 9.376 vínculos únicos (9.364 em serviços aprovados) |
| Fornecedores | 111 | 111 (nenhum perdido) |
| Prefixos de descrição (`SV `, `SV-`, `H-`...) | 3 convenções misturadas | removidos do nome padronizado |
| Nomes acima de 40 caracteres no catálogo | — | **0** |
| Códigos repetidos no catálogo | — | **0** |
| Mesmo nome repetido na mesma categoria | — | **0** |

**Catálogo por categoria:** Civil 2.212 · Facilities 2.018 · Eletromecânica 1.496 · Materiais 1.137 · Insumos 460.
**Status:** 7.311 aprovados e 12 em revisão (o mesmo número de código existe em duas categorias).

**O que ainda pode reduzir:** 974 pares de serviços *parecidos, mas não idênticos* foram separados para decisão da Gerdau (99 de confiança alta, 409 média e 466 baixa). Se todos fossem aprovados o catálogo iria a 6.459 serviços (−11,8%), mas esse é um **teto**: a faixa baixa mistura serviços realmente diferentes. Por isso nada foi unificado sem a decisão da Gerdau (veja [Metodologia](docs/04-metodologia-do-saneamento.md)).

## 4. Como o sistema funciona em 30 segundos

1. O comprador digita o nome do serviço.
2. Se existe, ele escolhe o serviço do catálogo e segue. **Nada novo é criado.**
3. Se não existe, o sistema mostra os serviços parecidos (nota de 60% ou mais) para conferir.
4. Se mesmo assim for novo, o comprador preenche o formulário. O filtro **bloqueia** nome repetido na categoria ou com **90% ou mais** de semelhança a um existente, e recusa nomes com mais de 40 caracteres.
5. O que passa entra como **PENDENTE**, com código gerado pelo sistema. O administrador **aprova** ou **reprova** (com motivo).

A checagem roda em memória e leva cerca de 8 ms para ~8 mil serviços (medição registrada no código, com a base real).

## 5. Mapa da documentação

| Documento | Para quem | Conteúdo |
|---|---|---|
| [01 · Guia do comprador](docs/01-guia-do-comprador.md) | Quem cria e consulta códigos | Passo a passo das telas, pesquisa de vários serviços de uma vez, avisos e bloqueios |
| [02 · Guia do administrador](docs/02-guia-do-administrador.md) | Quem administra o catálogo | Alterar serviços, aprovações, vínculos com pesquisa, importação e usuários (login por e-mail, um usuário por fornecedor) |
| [03 · Regra de nome e padronização](docs/03-regra-de-nome-e-padronizacao.md) | Compras, dados | Formato do código, limite de 40, dicionário de siglas, antes e depois |
| [04 · Metodologia do saneamento](docs/04-metodologia-do-saneamento.md) | IA & Dados, banca | Diagnóstico, níveis de similaridade, travas, o que ficou para validação humana |
| [05 · Fluxo de governança](docs/05-fluxo-de-governanca.md) | Todos | Consulta → sugestão → validação/bloqueio → aprovação, com diagrama |
| [06 · Guia técnico](docs/06-guia-tecnico.md) | Quem vai rodar e manter | Como subir, banco, telas e rotas, importação, testes |
| [07 · Limitações e próximos passos](docs/07-limitacoes-e-proximos-passos.md) | Gerdau, banca | Hipóteses a confirmar e pendências |

## 6. Começando rápido (quem vai rodar o sistema)

```bash
# Java 21 e um banco PostgreSQL (ex.: Supabase)
export DB_URL="jdbc:postgresql://<host>:5432/<banco>"
export DB_USER="<usuario>"
export DB_PASSWORD="<senha>"
export ADMIN_EMAIL="<e-mail do admin>"            # padrão: admin@gerdau.local
export ADMIN_SENHA="<senha do admin>"
export COMPRADOR_EMAIL="<e-mail do comprador>"    # padrão: comprador@gerdau.local
export COMPRADOR_SENHA="<senha do comprador>"

./mvnw spring-boot:run
```

Abra `http://localhost:8080`, entre com o **e-mail** do administrador e importe o catálogo em **Admin → Importação**. Detalhes no [Guia técnico](docs/06-guia-tecnico.md).

## 7. Estrutura do repositório

```
desafiogc/                  sistema (Spring Boot)
  src/main/java/...         controller, service, repository, model
  src/main/resources/       telas (Thymeleaf), CSS/JS, normalizacao/regras.csv
  docs/                     importacao.md, modelos de CSV
dados/
  Gerdau_saneamento_atualizado.xlsx
  pares_semelhantes_para_revisao.xlsx
docs/                       esta documentação
```
