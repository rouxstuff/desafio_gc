package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: PerfilAcesso.java
 * TIPO: classe utilitária (final, construtor privado, só métodos estáticos). Visível apenas no pacote
 *   controller.
 * FUNÇÃO: responder às perguntas de perfil do usuário logado, em um só lugar.
 * MÉTODOS:
 * - ehAdmin(auth): verdadeiro se o usuário autenticado tem o perfil ROLE_ADMIN; falso para usuário nulo.
 */

import org.springframework.security.core.Authentication;

final class PerfilAcesso {

    private PerfilAcesso() { }

    static boolean ehAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
