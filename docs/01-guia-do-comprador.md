# 01 · Guia do comprador

Este guia mostra como consultar, solicitar e contratar serviços no sistema. Não é preciso conhecer o projeto nem programação.

[← Voltar ao README](../README.md)

## Antes de começar

- **Pedir acesso:** na tela de login, clique em **Solicitar cadastro**, informe nome, e-mail e uma senha (mínimo 8 caracteres) e envie. O pedido fica **em análise** até o administrador aprovar. Só depois disso o login funciona.
- **Entrar:** use o **e-mail** e a senha. O botão **Mostrar/Ocultar** deixa você conferir o que digitou.
- **Perfis:** o sistema tem **Comprador** (consulta, solicita e contrata) e **Administrador** (altera serviços, aprova, vincula e administra). Este guia é do Comprador.
- A ideia central é simples: **procure primeiro, crie só se não existir.**

## Menu do comprador

| Menu | Para que serve |
|---|---|
| **Consulta** | Procurar um serviço pelo nome |
| **Catálogo** | Lista paginada de todos os serviços aprovados, com busca por nome ou código |
| **Solicitar serviço** | Pedir um serviço novo (só quando a consulta não achou) |
| **Meus serviços** | Os serviços que você já contratou, com data |

## 1. Procurar um serviço

1. Na tela **Consulta**, digite o nome do serviço (ex.: `eletricista`, `ajudante hora extra`).
2. O sistema entende abreviações: `H EXTRA`, `HORA EXTRA` e `HE` são tratados como a mesma coisa.
   **Para pesquisar vários de uma vez, separe com ponto e vírgula** (ex.: `ajudante;mecanico;almoço`, até 10 termos). A lista junta os resultados, mostra qual pesquisa achou cada serviço e avisa quais termos não acharam nada, com atalho para cadastrar.
3. O que pode acontecer:

| Resultado | O que fazer |
|---|---|
| **Um serviço encontrado** | Você vai direto para a tela de detalhes |
| **Vários encontrados** | Aparece uma lista com código, nome, categoria e status. Escolha o seu |
| **Nenhum encontrado** | Aparece "Serviço não encontrado" com sugestões parecidas. Veja a seção 3 |

> **Só serviços aprovados aparecem para você.** Serviço pendente (ainda não aprovado) ou reprovado fica escondido na Consulta, no Catálogo e nas buscas, e não pode ser contratado.

## 2. Contratar um serviço que já existe

Na tela de detalhes você vê o código, a categoria e a unidade de medida.

- Informe a **quantidade** (obrigatória).
- Marque os **adicionais** do contrato, se algum se aplica (hora extra, noturno, periculosidade etc.).
- Se quiser, escreva uma **observação** (até 200 caracteres).
- A **unidade de medida vem do catálogo e você não pode escolhê-la**: só o administrador define a unidade de um serviço. Se o serviço ainda não tem unidade, a tela avisa e a quantidade fica indisponível até o administrador definir.
- Se você digitou algo a mais junto do nome (por exemplo, `eletricista hora extra`), a tela avisa que isso **não faz parte do nome**. Marque nos adicionais ou anote na observação. O serviço continua o mesmo.

**Regras da quantidade**

| Unidade | Formato |
|---|---|
| ST (peça), ZCJ (conjunto), SV (serviço) | Número inteiro (ex.: `10`) |
| Demais (H, M, M2, M3, KG, MON, TAG...) | Decimal com **vírgula**, até 3 casas (ex.: `2,5`) |

Sempre maior que zero. **Não use ponto**: `1.500` seria confundido com 1,5.

Ao confirmar, a contratação é **gravada** e aparece em **Meus serviços**, com data, quantidade, adicionais e observação. Se o seu usuário está ligado a um comprador da base, o **vínculo** entre esse comprador e o serviço também é criado automaticamente.

## 3. Quando o serviço realmente não existe

Na tela **Serviço não encontrado**, confira a lista "Será que é um destes?". Cada sugestão traz o código, o nome e a **aderência** (percentual de semelhança).

- Se algum serve, escolha-o. É o caminho preferido, porque evita duplicado.
- Se nenhum serve, clique em **Cadastrar novo serviço**.

## 4. Solicitar um novo serviço

Preencha o formulário:

| Campo | Como preencher |
|---|---|
| **Nome do serviço (SAP)** | Só o nome do cargo ou da função, **até 40 caracteres**. O contador mostra quanto falta. Hora extra, insalubridade e outros adicionais **não entram no nome** |
| **Categoria** | Obrigatória (Civil, Eletromecânica, Facilities, Materiais ou Insumos) |
| **Unidade de medida / de custo** | Opcionais |
| **Quantidade** e **Observação** | Opcionais |
| **Descrição** | Opcional, até 100 caracteres |

O **código é gerado pelo sistema** (formato `PREFIXO-NÚMERO`). Você não digita código.

Se o sistema recusar o pedido ou achar serviços parecidos, o **aviso (faixa vermelha)** e **"Já existe algum destes?" (faixa amarela)** aparecem **no topo da tela**, sem precisar rolar a página. Clique em uma sugestão para abrir o serviço.

## 5. Avisos e bloqueios

| Mensagem | O que significa | O que fazer |
|---|---|---|
| *O nome passou de 40 caracteres (limite do SAP)* | O SAP não aceita mais que 40 | Encurte o nome, tirando adicionais e palavras de ligação |
| *Já existe um serviço com este nome na categoria X* | O **mesmo nome** (ignorando maiúsculas e acentos) já existe, **na mesma categoria ou em outra**. Quando é em outra categoria, a mensagem mostra o código do serviço existente | Use o serviço que já existe |
| *Este serviço já existe: CÓDIGO - NOME* | Semelhança de **90% ou mais** com um serviço da mesma categoria. É **duplicado** | Use o código indicado |
| *Escolha a categoria* / *Digite o nome do serviço* | Campo obrigatório vazio | Preencha |
| Quantidade inválida | Formato errado para a unidade | Veja a tabela da seção 2 |

**Nem toda sugestão bloqueia.** Parecidos entre 60% e 89% só **aparecem como sugestão**. Isso é de propósito: nomes que diferem por hora extra × hora normal, por medida (25MM × 50MM) ou por uma palavra realmente diferente (EXT × INT) são serviços distintos.

## 6. O que acontece depois da solicitação

O serviço entra como **PENDENTE** e **só o administrador o vê**. Ele analisa:

- **Aprovado:** entra no catálogo e passa a aparecer para você, podendo ser contratado.
- **Reprovado:** fica registrado com o motivo e **continua escondido de você**.

Enquanto está pendente, você não consegue achá-lo na Consulta nem contratá-lo. Isso é esperado.

## 7. Perguntas rápidas

**Digitei o nome certo e o sistema não achou.** Tente só o cargo (`soldador`) ou sem abreviações. A busca olha o nome inteiro e as formas normalizadas. Lembre que serviços pendentes não aparecem.

**Meu nome ficou grande demais.** Tire adicionais (hora extra, noturno, periculosidade) e palavras de ligação (de, da, do). Esses detalhes vão nos campos próprios.

**Pedi um serviço e ele não aparece na Consulta.** Ele está pendente. Quando o administrador aprovar, ele aparece.

**Contratei por engano.** Peça ao administrador para corrigir; hoje o comprador não cancela a contratação pela tela.
