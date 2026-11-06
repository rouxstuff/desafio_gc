# 06 · Guia técnico

Para quem vai subir, manter ou evoluir o sistema.

[← Voltar ao README](../README.md)

## 1. Tecnologias

| Item | Versão / detalhe |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Validation) |
| Telas | Thymeleaf (HTML) com CSS e JavaScript próprios, sem framework de front-end |
| Banco | PostgreSQL 18 (driver incluso) |
| Segurança | Spring Security com login por formulário e senhas em BCrypt |
| Build | Maven (`./mvnw`) |
| Execução | Docker Compose (`db` + `app`) |
| Testes | JUnit (Spring Boot Test) |

## 2. Como subir

### Com Docker (recomendado)

```bash
cd desafiogc
cp .env.example .env          # no Windows: copy .env.example .env
# edite o .env e troque DB_PASSWORD (use letras e números)
docker compose up -d --build
```

- O container `db` (PostgreSQL 18) **restaura o banco** a partir de `db/backup.dump` na primeira subida, usando `db/01-restore.sh`. Depois executa `db/02-limpar-almoco-duplicado.sql`.
- O container `app` só sobe quando o banco está saudável. A aplicação fica em **http://localhost:8080**.
- `docker compose down -v` **apaga o banco**; a próxima subida restaura do zero. Se mudar a senha do `.env` depois da primeira subida, faça isso.

### Sem Docker

Requisitos: Java 21 e um PostgreSQL **18** com o banco **restaurado a partir do dump** (o dump traz usuários, unidades, categorias, serviços, compradores e vínculos).

```bash
# 1. crie o banco e restaure o dump (PostgreSQL 18)
createdb -U <usuario> <banco>
pg_restore -U <usuario> -d <banco> --no-owner --no-privileges desafiogc/db/backup.dump

# 2. configure a conexão
export DB_URL="jdbc:postgresql://<host>:5432/<banco>"
export DB_USER="<usuario>"
export DB_PASSWORD="<senha>"

# 3. suba
./mvnw spring-boot:run        # no Windows: mvnw.cmd spring-boot:run
```

> O `pg_restore` precisa ser de versão compatível com o dump (PostgreSQL 18). Com um cliente mais antigo, o restore falha.

## 3. Configuração

O `application.yaml` lê o banco de variáveis de ambiente:

| Variável | Para quê |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL (obrigatória) |
| `DB_USER` / `DB_PASSWORD` | Credenciais do banco (obrigatórias) |

Outros pontos: pool de conexões com no máximo 5; `ddl-auto: update` (o Hibernate cria e ajusta as tabelas); inserts em lote de 50; upload de até 20 MB.

**O sistema não cria dados ao subir.** Unidades de medida, unidades de custo, categorias e usuários vêm do dump. Os logins de partida do dump são:

| Perfil | Login | Senha inicial |
|---|---|---|
| Administrador | `admin@gerdau.local` | `trocar-admin` |
| Comprador | `comprador@gerdau.local` | `trocar-comprador` |

**Troque essas senhas antes de qualquer uso real.** Em um banco vazio (sem o dump) não existe nenhum usuário, e o login é impossível.

## 4. Organização do código

Pacote base: `gerdauGrupo16.desafiogc`

```
controller/   telas e API REST (+ dto/)
service/      regras de negócio
  reguaAderencia/   NormalizadorTexto, PerfilTexto, ReguaAderencia
  importacao/       ImportacaoService, CsvLeitor, CodigoComPrefixo
  IndiceServicos    índice em memória
  ServicoService    criar, aprovar, reprovar, gerar código, visibilidade por perfil
  ContratacaoService  registra a contratação e cria o vínculo automático
  CadastroService   pedidos de cadastro (Solicitar cadastro)
  UsuarioService    criar usuário e ligar a um comprador (um usuário por comprador)
  QuantidadeServico validação da quantidade por unidade
  TermosBusca       separa a pesquisa da Consulta por ponto e vírgula (até 10 termos)
  EmailUsuario      normaliza (minúsculas) e valida o e-mail de login
repository/   acesso ao banco (Spring Data + projeções leves)
model/        entidades e enums
security/     SecurityConfiguration, WebConfiguration
```

### Modelo de dados

> Por compatibilidade com os dados já carregados, o banco **manteve os nomes antigos** `fornecedor`, `fornecedor_servico`, `fornecedor_id`. No sistema e nas telas o nome é **Comprador**.

| Tabela | Campos principais |
|---|---|
| `servico` | `codigo_servico` (PK, até 20), `nome_servico` (até 40), `descricao_servico` (até 100), `status` (PENDENTE / APROVADO / REJEITADO), `categoria_id`, `unidade_medida_id`, `unidade_custo_id`, `solicitado_por`, `motivo_rejeicao`. **Único:** (`categoria_id`, `nome_servico`) |
| `categoria` | `id`, `categoria_nome` (único), `prefixo` (único, 2 a 10 letras), `ultimo_numero` (contador) |
| `fornecedor` | o **comprador** da base: `id`, `codigo_fornecedor` (único), `nome_fornecedor` |
| `fornecedor_servico` | vínculo comprador × serviço: `id`, `fornecedor_id`, `codigo_servico`, `vinculado_em`. **Único:** (`fornecedor_id`, `codigo_servico`) |
| `contratacao` | confirmações do comprador (*Meus serviços*): `usuario_email`, `codigo_servico`, `nome_servico`, `unidade_medida`, `quantidade`, `adicionais`, `observacao`, `criada_em` |
| `unidade_medida`, `unidade_custo` | tabelas de apoio |
| `usuarios`, `usuario_roles` | e-mail (login, na coluna `usuario_nome`), senha em BCrypt, perfis (ADMIN, COMPRADOR) e `fornecedor_id` (opcional, só para o perfil Comprador; **único**) |

A checagem de **mesmo nome em outra categoria** é feita no código (`ServicoService`), não por restrição no banco. O nome único por categoria, o vínculo único por par e o comprador único por usuário também estão no banco.

### Índice em memória

`IndiceServicos` guarda todos os serviços com o texto já normalizado. Ele é **recarregado sozinho** na próxima consulta depois que um serviço é criado, aprovado ou reprovado, ou quando uma importação termina (a invalidação acontece só depois do *commit*). Se alguém alterar o banco por SQL direto, use **Importação → Recarregar índice**. **Limite do MVP:** funciona para uma instância da aplicação.

## 5. Importar por CSV (opcional)

Com o dump restaurado, o catálogo **já está carregado**. A importação serve para carregar um novo catálogo em banco vazio ou acrescentar serviços e vínculos. Quem faz isso é o administrador, em **Importação**. O detalhamento e os modelos de CSV estão em [`desafiogc/docs/importacao.md`](../desafiogc/docs/importacao.md) e `desafiogc/docs/modelos/`.

**Regras de segurança:** é **tudo ou nada** (um erro e nada é gravado), existe o modo **"Só simular"** que valida sem gravar, e reenviar o mesmo arquivo não duplica (códigos que já existem são pulados).

### Passo a passo

1. Faça backup: `pg_dump -Fc -U <usuario> <banco> > backup.dump`.
2. **Serviços:** envie o CSV com **Só simular** marcado, corrija o que o relatório apontar (ele mostra o número da linha) até zerar os erros, depois desmarque e envie.
3. **Vínculos:** mesmo processo. Só serviços `APROVADO` podem ser vinculados.
4. Confira no banco:

```sql
select status, count(*) from servico group by status;
select c.categoria_nome, c.prefixo, count(*) from servico s join categoria c on c.id = s.categoria_id group by 1, 2;
select count(*) from fornecedor;
select count(*) from fornecedor_servico;
```

### Cabeçalhos aceitos

O sistema lê CSV (separador `;` ou `,`; UTF-8). Os cabeçalhos são normalizados (sem acento, minúsculos).

**Serviços:** `codigo_servico`, `nome_servico`, `categoria`, `unidade_medida`, `status` e, para categorias que ainda não existem, `prefixo_categoria`.
**Vínculos:** `codigo_fornecedor` (o comprador) e `codigo_servico`.

Ao importar a partir da planilha `Gerdau_saneamento_atualizado.xlsx`, algumas colunas precisam ser renomeadas (ex.: `Nome padronizado (≤ 40 car.)` → `nome_servico`; `Código do serviço (novo)` → `codigo_servico`). Use o **código novo** (com prefixo).

**Pontos de atenção** (veja também [Limitações](07-limitacoes-e-proximos-passos.md)):

- O catálogo tem **12 serviços com status `EM REVISÃO`**, e o sistema só aceita `APROVADO`, `PENDENTE` ou `REJEITADO`. Com o status original a importação **falha inteira**.
- Os nomes terminados em `#NNNN` (241) são nomes cortados ambíguos: entram como estão, mas merecem revisão.

A importação acrescenta o prefixo da categoria ao código existente (`4057137` → `CIV-4057137`) e não duplica se já vier com prefixo. Depois da importação, o contador de cada categoria é ajustado para o **maior número existente**.

## 6. Telas e rotas

**Páginas (HTML)**

| Rota | Quem acessa | Função |
|---|---|---|
| `/login` | todos | Login |
| `/cadastro` | todos (sem login) | Solicitar cadastro |
| `/servicos` | logados | Consulta. Aceita vários termos separados por `;` |
| `/servicos/detalhes?codigo=` | Comprador | Detalhes e confirmação (contratar). Só abre serviço `APROVADO`. A unidade de medida vem do catálogo e o servidor ignora a que vier na requisição. O **Admin é redirecionado** para `/admin/servicos/{codigo}` |
| `/servicos/novo` | Comprador | Formulário de novo serviço |
| `/catalogo` | Comprador | Catálogo paginado (50 por página), só serviços aprovados. O Admin é redirecionado para `/admin/servicos` |
| `/contratacoes` | Comprador e Admin | Comprador: **Meus serviços**. Admin: aba **Contratações** (vínculos com data e confirmações dos usuários). `POST /contratacoes` (vincular) é só do Admin |
| `/admin/servicos` | Admin | Lista com busca por nome ou código, **inclusive pendentes e reprovados** |
| `/admin/servicos/{codigo}` | Admin | **Alterar serviço**: unidade de medida, unidade de custo e descrição |
| `/admin/solicitacoes` | Admin | Aprovar/reprovar (menu **Aprovações**) |
| `/admin/importacao` | Admin | Importar CSV e recarregar índice |
| `/admin/usuarios` | Admin | Aprovar/recusar pedidos de cadastro, criar usuários e ligar usuário a comprador |

**API REST**

Pendentes e reprovados só são devolvidos ao administrador. A visibilidade é decidida no serviço (`ServicoService.visivelPara`), não só na tela. Criar serviço (`POST /api/servicos`) exige o perfil Comprador. A API é ignorada pelo CSRF para facilitar testes (veja [Limitações](07-limitacoes-e-proximos-passos.md)).

| Método e rota | Função |
|---|---|
| `GET /api/servicos?pagina=&tamanho=` | Lista paginada (máx. 500 por página) |
| `GET /api/servicos/busca?nome=` | Busca por nome ou código |
| `GET /api/servicos/categoriaNomeServico` | Filtro por categoria e/ou nome |
| `GET /api/servicos/sugestoes?categoriaId=&nome=` | Serviços parecidos com a nota da régua |
| `POST /api/servicos` | Cria serviço `PENDENTE`. Respostas: 201, 400, 404, 409 |
| `GET /categoria`, `GET /categoria/busca` | Categorias |
| `GET /unidades-medida`, `GET /unidades-custo` | Tabelas de apoio |
| `GET /comprador`, `GET /comprador/codigoComprador` | Compradores (só Admin) |
| `POST /comprador/{compradorId}/servicos/{codigoServico}` | Vincula (201, 404, 409) (só Admin) |
| `GET /comprador/{compradorId}/servicos` | Serviços do comprador (só Admin) |

Exemplo: `GET /api/servicos/sugestoes?nome=Ajudante Geral` devolve uma lista com `codigoServico`, `nomeServico` e `aderenciaPercentual`.

## 7. Limpeza do ALMOCO PREMIUM e desvínculo

- **`db/02-limpar-almoco-duplicado.sql`** remove os 8 serviços `ALMOCO PREMIUM` duplicados (séries 03 e 04), movendo vínculos e contratações para os códigos mantidos (série 02) e renomeando os mantidos para `ALMOCO PREMIUM (faixa)`. É idempotente. No Docker roda sozinho em banco novo; em banco que já está rodando:

```bash
docker compose exec -T db psql -U gerdau -d desafiogc < db/02-limpar-almoco-duplicado.sql
```

- **Desvincular um comprador de um serviço** (por exemplo, para refazer uma demonstração): apague o vínculo em `fornecedor_servico` e a contratação em `contratacao` do serviço e do usuário em questão. Faça backup antes e confira o `WHERE`.

## 8. Desempenho

- **Checagem de duplicado:** índice em memória, ~8 ms para ~8 mil serviços (medição registrada no código).
- **Problema N+1 corrigido** (registro em `desafiogc/docs/ALTERACOES-PERFORMANCE.md`): a aba de vínculos fazia ~9 mil consultas, uma por vínculo. Agora usa uma consulta leve (id e rótulo), as listas usam projeção leve, `FornecedorServico` passou a `LAZY`, e a importação de vínculos grava em **lotes JDBC de 500**.
- Para conferir: com `show-sql: true`, abra a aba Contratações e conte as queries no log (esperado: poucas, não milhares).

## 9. Testes automatizados

Há testes de unidade para a régua de aderência (nomes iguais, siglas, medidas, truncamento, HE × HN, maior × menor), prefixo de código, leitura de CSV, regras de quantidade, separação da pesquisa por `;` (`TermosBuscaTest`) e validação do e-mail (`EmailUsuarioTest`), além do teste de carga do contexto Spring (que precisa de banco acessível).

```bash
./mvnw test
```

> Os testes **não foram executados** pela equipe que escreveu esta documentação (o ambiente não tinha acesso às dependências do Maven). Rode-os antes da apresentação.
