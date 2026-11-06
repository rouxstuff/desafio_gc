package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: LoginController.java
 * TIPO: controller de telas (@Controller).
 * FUNÇÃO: entrega a página de login. A autenticação em si é feita pelo Spring Security
 *   (SecurityConfiguration).
 * MÉTODOS:
 * - login(): GET /login. Abre a página servicos/login.
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(){
        return "servicos/login";
    }
}
