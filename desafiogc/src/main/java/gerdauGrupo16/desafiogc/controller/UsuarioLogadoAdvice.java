package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: UsuarioLogadoAdvice.java
 * TIPO: @ControllerAdvice. Atua sobre todos os controllers de tela.
 * FUNÇÃO: coloca em todo model duas informações que o menu das páginas precisa, sem cada controller repetir
 *   isso.
 * MÉTODOS:
 * - usuarioLogado(auth): atributo usuarioLogado, o e-mail de quem está logado (ou null).
 * - ehAdmin(auth): atributo ehAdmin, verdadeiro para o administrador; o menu usa para mostrar as abas do
 *   administrador.
 */

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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
