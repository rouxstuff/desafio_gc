package gerdauGrupo16.desafiogc.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Entrega "usuarioLogado" e "ehAdmin" prontos para todas as páginas (usado no menu do layout). */
@ControllerAdvice
public class UsuarioLogadoAdvice {

    @ModelAttribute("usuarioLogado")
    public String usuarioLogado(Authentication auth) {
        return auth != null ? auth.getName() : null;
    }

    @ModelAttribute("ehAdmin")
    public boolean ehAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
