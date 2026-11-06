# 02 · Guia do administrador

Este guia é para quem administra o catálogo: altera as características dos serviços, aprova serviços novos, vincula compradores a serviços, importa o catálogo saneado e gerencia os usuários. Tudo isso fica nos itens do menu que **só o administrador vê**.

[← Voltar ao README](../README.md)

| Menu | Para que serve |
|---|---|
| **Consulta** | Localiza serviços. O administrador **não contrata nem solicita**: ao clicar em um resultado, vai para a tela **Alterar serviço** |
| **Serviços** | Lista todos os serviços (com pesquisa por nome ou código, **inclusive pendentes e reprovados**) e permite **alterar as características** de cada um. Substitui o **Catálogo**, que o administrador não vê |
| **Contratações** | Vincular compradores a serviços aprovados, pesquisar um serviço e ver quem o usa e desde quando, e conferir as confirmações dos usuários |
| **Aprovações** | Aprovar ou reprovar os serviços solicitados |
| **Importação** | Carregar o catálogo saneado e os vínculos por arquivo CSV |
| **Usuários** | Aprovar pedidos de cadastro, criar usuários e ligar cada usuário a um comprador da base |

## 1. Entrar no sistema

O login é por **e-mail e senha**. O pacote Docker já traz dois usuários de partida (`admin@gerdau.local` e `comprador@gerdau.local`, com senhas iniciais `trocar-admin` e `trocar-comprador`). **Troque as senhas antes de qualquer uso real.**

## 2. Serviços: alterar as características de um serviço

O administrador não contrata serviço; ele **mantém o cadastro**. Há dois caminhos para a tela **Alterar serviço**:

- clicar em **Serviços** no menu, pesquisar pelo nome ou código e clicar em **Alterar**;
- clicar em um resultado da **Consulta**.

A lista da aba **Serviços** mostra código, serviço, categoria, unidade de medida e status, 50 por página. Na tela você altera:

| Campo | Observação |
|---|---|
| **Unidade de medida** | Passa a valer para quem contratar o serviço. **Só o administrador altera**. "Sem unidade" remove |
| **Unidade de custo** | Opcional; "Sem unidade de custo" remove |
| **Descrição** | Opcional, até 100 caracteres |

O **nome, a categoria e o código não mudam** nessa tela (o nome é a base da checagem de duplicados) e o **status** muda em **Aprovações**. Serviços pendentes e rejeitados também podem ser alterados.

> Serviço aprovado **sem unidade de medida** não permite que o comprador informe a quantidade, porque a quantidade depende da unidade. Defina a unidade aqui.

## 3. Aprovações

Todo serviço solicitado por um comprador entra como **PENDENTE** e aparece em **Aprovações**, com código, nome, categoria e quem solicitou.

- **Aprovar:** o serviço vira `APROVADO`, passa a aparecer para os compradores e pode ser contratado e vinculado.
- **Reprovar:** o **motivo é obrigatório**. O serviço vira `REJEITADO`.
- **Pendentes e reprovados só o administrador vê.** Para o comprador eles não aparecem na Consulta nem no Catálogo e não podem ser contratados.
- Um pedido só pode ser decidido uma vez. Tentar decidir de novo mostra *"Este pedido já foi decidido"*.

Antes de aprovar, vale conferir se o nome não é parecido com um serviço que já existe. O sistema já bloqueia o **mesmo nome em qualquer categoria** e a semelhança de **90% ou mais na mesma categoria**; o que passou foi considerado diferente, mas a última palavra é sua.

## 4. Contratações

Esta aba reúne tudo sobre quem usa cada serviço. Um mesmo serviço do catálogo pode ser usado por vários compradores, **sem criar código duplicado**.

**Nova contratação (vincular um comprador a um serviço)**

1. Escolha o **Comprador**.
2. Escolha o **Serviço** (só aparecem os aprovados).
3. Clique em **Vincular comprador**.

O sistema recusa se o serviço não estiver aprovado ou se aquele comprador já estiver vinculado a ele.

**Compradores por serviço (pesquisar)**

1. Use **Pesquisar serviço** e digite parte do **nome ou do código** (ex.: `soldador`, `CIV-4057137` ou só `4057137`). A busca ignora maiúsculas e acentos.
2. Clique em **Buscar**. A lista passa a mostrar só os serviços encontrados (até 50).
3. Escolha o serviço. A tela mostra uma tabela com os **compradores vinculados e a data do vínculo**. Vínculos vindos da importação inicial aparecem como "Importação inicial".

Se a pesquisa encontrar um único serviço, ele já abre sozinho. **Limpar** volta ao estado inicial.

**Confirmações dos usuários:** mais abaixo, a tabela lista as contratações feitas pelos compradores (as 200 mais recentes), com data, e-mail, serviço, quantidade, adicionais e observação.

**O vínculo também nasce sozinho:** quando um usuário ligado a um comprador da base contrata um serviço, o vínculo é criado automaticamente, com a data.

> Quando o comprador da base não tem nome cadastrado (caso da base anonimizada), o rótulo mostra só os **8 primeiros caracteres** do código. Na aba **Usuários** o código aparece completo.

## 5. Importação

Usada para carregar o catálogo saneado e os vínculos. O passo a passo completo está no [Guia técnico](06-guia-tecnico.md). Em resumo:

1. Faça backup do banco.
2. Envie o CSV de **serviços** com **Só simular** marcado e corrija o que o relatório apontar (ele diz o número da linha).
3. Desmarque **Só simular** e envie de novo. É **tudo ou nada**: se houver erro, nada é gravado.
4. Faça o mesmo com o CSV de **vínculos** (só serviços aprovados aceitam vínculo).
5. Se alterar o banco por fora ou o dicionário de siglas, use **Recarregar índice**.

## 6. Usuários

### Pedidos de cadastro

Quem não tem acesso usa **Solicitar cadastro** no login. Os pedidos aparecem em **Usuários**, na seção **Solicitações de cadastro**. Para cada pedido, clique em **Aprovar** (o usuário passa a conseguir entrar, sempre com o perfil Comprador) ou **Recusar**.

### Perfis

| Perfil | Pode | Comprador da base |
|---|---|---|
| **Administrador** | Tudo o que este guia descreve | Não tem |
| **Comprador** | Consultar, solicitar e contratar serviços | Opcional: o comprador da base a que o usuário pertence |

### Criar um usuário

1. Em **Novo usuário**, informe o **e-mail** (será o login) e uma **senha provisória** de pelo menos 8 caracteres.
2. Escolha o **perfil**. Para Comprador, você pode escolher **"Ligar a um comprador da base (opcional)"**: a lista mostra **só os que ainda não têm usuário**.
3. Clique em **Criar usuário** e passe a senha provisória ao usuário por um canal seguro.

O e-mail é guardado em minúsculas. **Cada comprador da base tem no máximo um usuário.**

### Vincular ou trocar o comprador de um usuário

Na tabela **Usuários cadastrados**, os usuários de perfil Comprador têm uma lista com o botão **Vincular** (quando ainda não têm comprador da base) ou **Trocar**. A lista mostra os compradores livres. Ligar o usuário a um comprador é o que faz o vínculo nascer sozinho quando ele contrata.

### Mensagens que você pode ver

| Mensagem | O que fazer |
|---|---|
| *Informe um e-mail válido* | Use o formato `nome@empresa.com` |
| *Já existe um usuário com o e-mail …* | Esse e-mail já tem acesso; use outro |
| *A senha deve ter pelo menos 8 caracteres* | Digite uma senha maior (no máximo 72 bytes) |
| *Este comprador já tem um usuário: …* | Cada comprador da base tem um único usuário. Troque o comprador ou use o usuário que já existe |
| *Só usuários com perfil Comprador podem ser vinculados a um comprador* | O Administrador não tem comprador |

### O que ainda não existe

O usuário **não troca a própria senha**, não há redefinição por e-mail nem como desativar um usuário. Se alguém esquecer a senha, hoje é preciso corrigir direto no banco. Isso está em [Limitações](07-limitacoes-e-proximos-passos.md).

## 7. Boas práticas

- **Senhas padrão:** troque `trocar-admin` e `trocar-comprador` antes de usar.
- **Backup antes de importar** e **simular antes de gravar**.
- **Aprove com critério:** um código aprovado vira parte do catálogo. É mais barato recusar agora do que unir códigos depois.
- Depois de mexer no banco por SQL direto, use **Recarregar índice** para as sugestões enxergarem o dado novo.
