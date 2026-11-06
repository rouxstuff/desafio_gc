package gerdauGrupo16.desafiogc.model.enumeration;

/*
 * ARQUIVO: Roles.java
 * TIPO: enum (perfis de acesso).
 * FUNÇÃO: lista os perfis do sistema.
 * VALORES: ADMIN (administra catálogo, aprovações, vínculos, importação e usuários); FORNECEDOR (consulta e
 *   solicita serviços, ligado a um fornecedor); COMPRADOR (consulta e solicita serviços).
 */

public enum Roles {
    ADMIN,
    FORNECEDOR,
    COMPRADOR
}
