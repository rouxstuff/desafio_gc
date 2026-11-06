package gerdauGrupo16.desafiogc.model.enumeration;

/*
 * ARQUIVO: Roles.java
 * TIPO: enum (perfis de acesso).
 * FUNÇÃO: lista os perfis do sistema.
 * VALORES: ADMIN (administra catálogo, aprovações, vínculos, importação e usuários; não contrata serviço);
 *   COMPRADOR (consulta, solicita e contrata serviços; pode estar ligado a um comprador da base).
 */

public enum Roles {
    ADMIN,
    COMPRADOR
}
