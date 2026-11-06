package gerdauGrupo16.desafiogc.model.enumeration;

/*
 * ARQUIVO: StatusCadastro.java
 * TIPO: enum (situação de uma solicitação de cadastro de usuário).
 * FUNÇÃO: acompanha a solicitação desde o pedido até a decisão do administrador.
 * VALORES: PENDENTE (aguardando análise; a pessoa ainda não consegue entrar); APROVADO (o usuário foi
 *   criado); REJEITADO (recusado, com motivo).
 */

public enum StatusCadastro {
    PENDENTE,
    APROVADO,
    REJEITADO
}
