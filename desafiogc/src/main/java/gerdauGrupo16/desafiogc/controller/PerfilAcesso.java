package gerdauGrupo16.desafiogc.controller;

import org.springframework.security.core.Authentication;

/** Pergunta única "quem está logado é administrador?", usada pelas telas e pela API para decidir o que mostrar. */
final class PerfilAcesso {

    private PerfilAcesso() { }

    static boolean ehAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
