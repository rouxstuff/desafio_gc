<p align='center'>
    <img src='./Python-DiagnosticoGerdau/assets/logo-gerdau.png' width='150'/>
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

## Sumário

1. [O problema](#1-o-problema)
2. [O que entregamos](#2-o-que-entregamos)
3. [Resultados em números](#3-resultados-em-números)
4. [Como o sistema funciona em 30 segundos](#4-como-o-sistema-funciona-em-30-segundos)
5. [Como rodar (Docker, recomendado)](#5-como-rodar-docker-recomendado)
6. [Como rodar sem Docker](#6-como-rodar-sem-docker)
7. [Perfis e primeiro acesso](#7-perfis-e-primeiro-acesso)
8. [Testes](#8-testes)
9. [Problemas comuns](#9-problemas-comuns)
10. [Mapa da documentação](#10-mapa-da-documentação)
11. [Estrutura do repositório](#11-estrutura-do-repositório)
12. [Tecnologias](#12-tecnologias)

## 1. O problema

Os contratos de serviço da Gerdau usam códigos para identificar cada serviço contratado. Hoje cada comprador cria os próprios códigos, sem filtro nem conferência. Com o tempo isso gerou milhares de códigos repetidos, escritos de formas diferentes e limitados a 40 caracteres no SAP, o que corta muitos nomes no meio.

## 2. O que entregamos

| # | Entrega | Onde está |
|---|---|---|
| 1 | **Catálogo saneado e padronizado** de serviços, com código novo no formato `PREFIXO-CÓDIGO` | planilha `Gerdau_saneamento_atualizado.xlsx` |
| 2 | **Lista de pares parecidos para a Gerdau decidir** (unificar ou manter separado) | planilha `pares_semelhantes_para_revisao.xlsx` e o PDF "Unificação por semelhança" |
| 3 | **Sistema web de entrada de novos códigos** (consulta, sugestão, bloqueio de duplicados, aprovação e contratação) | pasta `desafiogc/` (Java + Spring Boot) |
| 4 | **Pacote Docker** com o banco já carregado, para subir com um comando | `desafiogc/` (`docker-compose.yml`, `Dockerfile`, `db/`) |
| 5 | **Documentação** para o time da Gerdau usar e repetir o processo | este material |

### Funcionalidades do sistema

O sistema tem **dois perfis**: **Comprador** e **Administrador**.

**Comprador**
- **Cadastro com aprovação:** quem não tem acesso clica em *Solicitar cadastro* no login; o pedido fica em análise até o administrador aprovar.
- **Consulta** de serviços por nome, com tolerância a siglas e **pesquisa de vários termos de uma vez** (separados por `;`).
- **Sugestão e bloqueio** de duplicados na criação (≥ 60% sugere; ≥ 90% de semelhança na mesma categoria bloqueia; **nome idêntico bloqueia em qualquer categoria**), com código gerado pelo sistema. O aviso e as sugestões aparecem no topo da tela.
- **Contratação:** confirma um serviço aprovado, com quantidade, adicionais e observação.
- **Meus serviços:** lista as contratações do próprio usuário, com data.
- **Catálogo** paginado com busca por nome ou código. O comprador só vê serviços **APROVADOS**: pendentes e reprovados ficam escondidos e não podem ser contratados.

**Administrador**
- **Aprovação** de serviços novos, com motivo obrigatório na reprovação. Serviço pendente ou reprovado **só o administrador vê**: Consulta e Catálogo mostram só serviços aprovados.
- **Serviços:** substitui o Catálogo para o administrador, com a mesma lista e a opção **Alterar** (unidade de medida, unidade de custo e descrição). O administrador não contrata serviço.
- **Contratações:** liga um comprador a um serviço aprovado, pesquisa um serviço por nome ou código e vê os compradores vinculados com a **data do vínculo**, além das confirmações dos usuários. O vínculo também nasce sozinho quando um usuário ligado a um comprador contrata um serviço.
- **Importação** do catálogo saneado e dos vínculos por CSV (tudo ou nada, com simulação antes de gravar).
- **Usuários:** login por **e-mail**; cria usuários, aprova ou recusa pedidos de cadastro e liga cada usuário Comprador a um comprador da base (**um usuário por comprador**).

> **Limpeza posterior:** a planilha saneada tem 7.323 códigos. Depois dela, 8 serviços `ALMOCO PREMIUM` duplicados (séries 03 e 04) foram removidos, mantendo a série 02 (tabela De-Para em `depara_almoco_removidos.csv`; script `docs/limpar-almoco-duplicado.sql`).

> **Nomes:** na planilha original os compradores aparecem como "fornecedor". No sistema o nome é **Comprador**. No banco, as tabelas e colunas mantêm o nome antigo (`fornecedor`, `fornecedor_servico`), para os dados continuarem valendo.

## 3. Resultados em números

| Indicador | Antes | Depois |
|---|---|---|
| Códigos de serviço distintos | 7.937 | **7.315** (−622, **−7,8%**) |
| Vínculos comprador × serviço | 9.795 linhas | 9.368 vínculos únicos (9.356 em serviços aprovados) |
| Compradores | 111 | 111 (nenhum perdido) |
| Prefixos de descrição (`SV `, `SV-`, `H-`...) | 3 convenções misturadas | removidos do nome padronizado |
| Nomes acima de 40 caracteres no catálogo | — | **0** |
| Códigos repetidos no catálogo | — | **0** |
| Mesmo nome repetido na mesma categoria | — | **0** |

**Catálogo por categoria:** Civil 2.212 · Facilities 2.018 · Eletromecânica 1.496 · Materiais 1.137 · Insumos 452.
**Status:** 7.303 aprovados e 12 em revisão (o mesmo número de código existe em duas categorias).

**O que ainda pode reduzir:** 974 pares de serviços *parecidos, mas não idênticos* foram separados para decisão da Gerdau (99 de confiança alta, 409 média e 466 baixa). Se todos fossem aprovados o catálogo iria a 6.451 serviços (−11,8%), mas esse é um **teto**: a faixa baixa mistura serviços realmente diferentes. Por isso nada foi unificado sem a decisão da Gerdau (veja [Metodologia](docs/04-metodologia-do-saneamento.md)).

## 4. Como o sistema funciona em 30 segundos

1. O comprador digita o nome do serviço.
2. Se existe, ele escolhe o serviço do catálogo e segue. **Nada novo é criado.**
3. Se não existe, o sistema mostra os serviços parecidos (nota de 60% ou mais) para conferir.
4. Se mesmo assim for novo, o comprador preenche o formulário. O filtro **bloqueia** nome idêntico a um existente (em qualquer categoria) ou com **90% ou mais** de semelhança a um da mesma categoria, e recusa nomes com mais de 40 caracteres.
5. O que passa entra como **PENDENTE**, com código gerado pelo sistema. O administrador **aprova** ou **reprova** (com motivo).
6. Aprovado, o serviço entra no catálogo e pode ser contratado e vinculado a compradores.

A checagem roda em memória e leva cerca de 8 ms para ~8 mil serviços (medição registrada no código, com a base real).

## 5. Como rodar (Docker, recomendado)

Sobe a aplicação e o banco PostgreSQL já com o catálogo saneado, com um comando.

**Requisitos:** Docker Desktop (Windows/Mac) ou Docker Engine + Compose (Linux). Java e Maven não precisam estar instalados: o build acontece dentro do Docker.

```bash
cd desafiogc
cp .env.example .env          # no Windows: copy .env.example .env
# edite o .env e troque DB_PASSWORD (use letras e números)
docker compose up -d --build
```

- A primeira vez leva alguns minutos: o build baixa as dependências e o banco restaura o catálogo (≈ 7,3 mil serviços, 111 compradores, 9,3 mil vínculos).
- Acompanhe com `docker compose logs -f` e procure `Started DesafiogcApplication`.
- Abra **http://localhost:8080**.

Comandos do dia a dia:

```bash
docker compose ps            # estado dos serviços
docker compose logs -f app   # log da aplicação
docker compose stop          # para (os dados ficam guardados)
docker compose up -d         # volta
docker compose down          # remove os containers (dados ficam no volume)
docker compose down -v       # APAGA tudo, inclusive o banco; a próxima subida restaura do zero
```

Limpeza dos serviços "ALMOCO PREMIUM" duplicados (séries 03 e 04) em um banco que já está rodando:

```bash
docker compose exec -T db psql -U gerdau -d desafiogc < db/02-limpar-almoco-duplicado.sql
```

Em uma subida nova (`docker compose down -v`), esse script roda sozinho depois da restauração.

Backup do banco:

```bash
docker compose exec db pg_dump -Fc -U gerdau desafiogc > backup_$(date +%F).dump
```

## 6. Como rodar sem Docker

**Requisitos:** Java 21 e um banco PostgreSQL (por exemplo, o do Supabase) vazio ou restaurado.

```bash
export DB_URL="jdbc:postgresql://<host>:5432/<banco>"
export DB_USER="<usuario>"
export DB_PASSWORD="<senha>"
export ADMIN_EMAIL="<e-mail do admin>"            # padrão: admin@gerdau.local
export ADMIN_SENHA="<senha do admin>"
export COMPRADOR_EMAIL="<e-mail do comprador>"    # padrão: comprador@gerdau.local
export COMPRADOR_SENHA="<senha do comprador>"

./mvnw spring-boot:run          # no Windows: mvnw.cmd spring-boot:run
```

As tabelas são criadas e atualizadas sozinhas na subida. Em um banco vazio, abra `http://localhost:8080`, entre com o **e-mail** do administrador e importe o catálogo em **Admin → Importação** (arquivos na pasta `docs/` do sistema). Detalhes no [Guia técnico](docs/06-guia-tecnico.md).

## 7. Perfis e primeiro acesso

Na subida com Docker, o sistema cria dois usuários de partida:

| Perfil | Login | Senha inicial |
|---|---|---|
| Administrador | `admin@gerdau.local` | `trocar-admin` |
| Comprador | `comprador@gerdau.local` | `trocar-comprador` |

> **Troque essas senhas antes de usar de verdade.** Quem precisa de acesso novo usa *Solicitar cadastro* na tela de login, e o administrador aprova em **Admin → Usuários**.

Para ver as duas telas ao mesmo tempo (administrador e comprador), use uma janela normal em `http://localhost:8080` e uma **janela anônima** em `http://127.0.0.1:8080`: o login fica preso ao endereço do navegador.

## 8. Testes

```bash
./mvnw test
```

Os testes cobrem a normalização de texto, a busca por vários termos, a leitura de CSV, o prefixo do código e a régua de aderência do filtro de duplicatas.

## 9. Problemas comuns

| Sintoma | O que fazer |
|---|---|
| O banco pede senha e a aplicação não conecta, ou fica em loop | A senha do `.env` mudou depois da primeira subida. Rode `docker compose down -v` e suba de novo (isso apaga o banco e restaura do zero) |
| `defina DB_PASSWORD no arquivo .env` | Falta criar o `.env` a partir do `.env.example` |
| Porta 8080 já em uso | Feche o programa que usa a porta ou troque a porta no `docker-compose.yml` (`"8081:8080"`) |
| A aplicação sobe antes de o banco terminar de restaurar | Aguarde: o `docker compose` espera o banco ficar saudável. Acompanhe com `docker compose logs -f` |
| Não consigo logar com dois usuários ao mesmo tempo | Use `localhost` em uma janela e `127.0.0.1` em uma janela anônima |
| `./mvnw: Permission denied` (Linux/Mac) | `chmod +x mvnw` |

## 10. Mapa da documentação

| Documento | Para quem | Conteúdo |
|---|---|---|
| [01 · Guia do comprador](docs/01-guia-do-comprador.md) | Quem cria e consulta códigos | Passo a passo das telas, pesquisa de vários serviços de uma vez, avisos e bloqueios |
| [02 · Guia do administrador](docs/02-guia-do-administrador.md) | Quem administra o catálogo | Alterar serviços, aprovações, contratações e vínculos, importação e usuários |
| [03 · Regra de nome e padronização](docs/03-regra-de-nome-e-padronizacao.md) | Compras, dados | Formato do código, limite de 40, dicionário de siglas, antes e depois |
| [04 · Metodologia do saneamento](docs/04-metodologia-do-saneamento.md) | IA & Dados, banca | Diagnóstico, níveis de similaridade, travas, o que ficou para validação humana |
| [05 · Fluxo de governança](docs/05-fluxo-de-governanca.md) | Todos | Consulta → sugestão → validação/bloqueio → aprovação, com diagrama |
| [06 · Guia técnico](docs/06-guia-tecnico.md) | Quem vai rodar e manter | Como subir, banco, telas e rotas, importação, testes |
| [07 · Limitações e próximos passos](docs/07-limitacoes-e-proximos-passos.md) | Gerdau, banca | Hipóteses a confirmar e pendências |

## 11. Estrutura do repositório

```
desafiogc/                  sistema (Spring Boot)
  src/main/java/...         controller, service, repository, model
  src/main/resources/       telas (Thymeleaf), CSS/JS, normalizacao/regras.csv
  src/test/java/...         testes
  db/                       backup.dump, restore e 02-limpar-almoco-duplicado.sql (Docker)
  docs/                     importacao.md, modelos de CSV
  Dockerfile, docker-compose.yml, .env.example
dados/
  Gerdau_saneamento_atualizado.xlsx
  pares_semelhantes_para_revisao.xlsx
docs/                       esta documentação
```

## 12. Tecnologias

Java 21 · Spring Boot (Web, Data JPA, Security) · Thymeleaf · PostgreSQL 18 · Docker Compose · Maven. Análise de dados e saneamento da base em Python (pandas, TF-IDF).
