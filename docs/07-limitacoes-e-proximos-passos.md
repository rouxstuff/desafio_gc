# 07 · Limitações e próximos passos

Esta página reúne o que ainda não está fechado, para a Gerdau e a banca terem uma visão honesta do estado da entrega.

[← Voltar ao README](../README.md)

## 1. Decisões que dependem da Gerdau

| # | Pendência | Por que importa |
|---|---|---|
| 1 | **Decidir os 974 pares parecidos** (planilha `pares_semelhantes_para_revisao.xlsx`) | Pode reduzir o catálogo até 6.451 serviços (teto de −11,8%). Começar pela aba ALTA (99 pares), depois MÉDIA. Hoje nenhuma decisão foi preenchida |
| 2 | **Confirmar a unidade** nos pares com alerta `UNIDADE DIFERENTE` (48 pares nas três abas) e nos 856 itens `UNIDADE_SUGERIDA` | O mesmo serviço aparece como H e como SV |
| 3 | **Confirmar nomes cortados** (1.899 possíveis, 240 ambíguos) | O limite de 40 caracteres do SAP corta descrições; só quem conhece o serviço sabe o nome completo |
| 4 | **Validar duas hipóteses** do diagnóstico: mobilização/desmobilização = **SV** (cobrança única) e "faturado por mês" = **MON** | Definem a unidade esperada para centenas de itens |
| 5 | **Itens "retroativo"** | Não têm unidade definida e ficaram fora da checagem |
| 6 | **Migrar contratos** do código antigo para o mantido, usando o De-Para | Unir códigos muda contratos |
| 7 | **Definir o destino dos 12 serviços `EM REVISÃO`** (mesmo número de código em duas categorias) | O sistema não tem esse status |
| 8 | **Decidir sobre nomes iguais em categorias diferentes** que já existem no catálogo (ex.: `ACRILICO 6MM` em Civil e em Materiais) | O sistema bloqueia novos casos, mas os existentes continuam |

## 2. Limitações da base

- **Só texto, grupo e unidade.** Não há preço, especificação técnica nem histórico de contrato para desempatar o que é igual ou diferente.
- **Unidade de medida pouco confiável** (22,6% dos itens de mão de obra verificáveis fora da esperada).
- **Concentração de compradores:** um comprador responde por cerca de 16% das linhas, então o padrão de escrita dele pesa nas regras.
- **Itens heterogêneos em Facilities** (cardápios, eventos, locações) não seguem o padrão de mão de obra.
- **Percentuais do diagnóstico** foram ajustados por amostra de **uma só pessoa**; leia-os com as faixas.
- **Planilhas desatualizadas após a limpeza do almoço:** `Gerdau_saneamento_atualizado.xlsx` (7.323 códigos) e `pares_semelhantes_para_revisao.xlsx` ainda trazem os 8 serviços `ALMOCO PREMIUM` removidos do sistema.

## 3. Limitações do sistema (MVP)

| Tema | Situação | Para produção |
|---|---|---|
| **Contratação** | A confirmação é gravada e aparece em *Meus serviços*, mas o comprador **não cancela nem edita** a contratação pela tela | Cancelamento/edição com histórico |
| **Instâncias** | O índice em memória funciona para **uma** instância | Com várias instâncias, trocar por cache compartilhado ou recarga sincronizada |
| **API e CSRF** | `/api/**` está **sem proteção CSRF** para facilitar testes (marcado no código para remover antes de produção) | Reativar CSRF ou usar autenticação por token |
| **Usuários** | Login por e-mail. O comprador pede acesso em *Solicitar cadastro* e o administrador aprova. Ainda **não há** troca de senha pelo próprio usuário, redefinição por e-mail, desativação de usuário nem confirmação de que o e-mail existe. As senhas iniciais dos usuários do dump são `trocar-...` | Troca obrigatória de senha no primeiro acesso, convite/redefinição por e-mail, desativar usuário e integração com o login corporativo |
| **Dados iniciais** | O sistema **não cria dados ao subir**: tudo vem do dump (`db/backup.dump`) | Migrações versionadas e carga inicial controlada |
| **Banco** | `ddl-auto: update` e `show-sql: true`; nomes de tabela antigos (`fornecedor`) | Usar migrações versionadas (Flyway/Liquibase), desligar o log de SQL e, se desejado, renomear as tabelas |
| **Dicionário de siglas** | `regras.csv` é um ponto de partida com os exemplos da Entrega 2 | Acrescentar o dicionário completo da trilha IA & Dados |
| **Régua** | Texto apenas; compara palavras e letras, **não o significado** | Se houver preço e especificação, usá-los como travas adicionais |
| **Segredos** | O dump e a configuração de desenvolvimento podem carregar senhas de teste | Trocar todas as senhas e regenerar o dump antes de qualquer uso real |
| **Testes** | Os testes automatizados existem, mas não foram executados pela equipe | Rodar `./mvnw test` e registrar o resultado |

## 4. Inconsistências encontradas nos materiais

Registradas aqui para serem corrigidas, sem alterar nenhum dado:

1. **Prefixo de Eletromecânica:** o dicionário de dados da planilha cita `ELE-`, mas o catálogo e o sistema usam **`ELETRO-`**. Vale o que está no catálogo.
2. **Categorias do catálogo:** o dicionário diz que o saneamento "consolidou Materiais e Insumos nas 3 categorias", mas o catálogo traz **cinco** categorias (inclui Materiais, 1.137, e Insumos, 452 no sistema). Vale o que está no catálogo.
3. **Vínculos:** são 9.368 únicos no sistema e 9.356 se contados só os serviços aprovados. O PDF "Unificação por semelhança" usa números anteriores à limpeza do almoço.
4. **Script `aplicar_decisoes.py`:** citado no PDF "Unificação por semelhança", não faz parte deste repositório.

## 5. Próximos passos sugeridos

**Curto prazo (para fechar a entrega)**

1. Rodar `./mvnw test` e registrar o resultado.
2. Trocar as senhas padrão e regenerar o dump sem senhas de teste.
3. Conferir as contagens do banco com as da seção "Resultados" do README (7.315 serviços; 9.356 vínculos em aprovados).
4. Entregar à Gerdau a planilha de pares com a instrução do PDF.

**Depois da resposta da Gerdau**

5. Aplicar as decisões, reimportar o catálogo e gerar o De-Para.
6. Tratar nomes cortados e unidades com os especialistas da Gerdau.

**Evolução do produto**

7. Cancelamento e histórico de contratações.
8. Autoatendimento de usuários (troca e redefinição de senha, desativar usuário).
9. Painel de indicadores: duplicados evitados (bloqueios), sugestões aceitas, tempo de aprovação.
10. Migrações de banco e ambiente de produção com mais de uma instância.
