package gerdauGrupo16.desafiogc.model.enumeration;

/*
 * ARQUIVO: StatusServico.java
 * TIPO: enum (situação do serviço).
 * FUNÇÃO: controla o ciclo de aprovação do serviço.
 * VALORES: PENDENTE (pedido novo, aguardando o administrador); APROVADO (faz parte do catálogo e aceita
 *   vínculos); REJEITADO (reprovado com motivo; só o administrador vê).
 */

public enum StatusServico {
    PENDENTE,
    APROVADO,
    REJEITADO
}
