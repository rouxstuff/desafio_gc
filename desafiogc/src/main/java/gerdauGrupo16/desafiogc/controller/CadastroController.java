package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: CadastroController.java
 * TIPO: controller de telas (@Controller), rota pública /cadastro (liberada em SecurityConfiguration).
 * FUNÇÃO: tela em que qualquer pessoa pede acesso ao sistema. O pedido não cria usuário: ele vai para a
 *   fila do administrador (aba Usuários) e a pessoa só consegue entrar depois da aprovação. A regra de
 *   negócio está em CadastroService.
 * MÉTODOS:
 * - tela(): GET /cadastro. Abre o formulário (template servicos/solicitar-cadastro).
 * - solicitar(nome, email, senha, confirmarSenha, redirect): POST /cadastro. Se der certo,
 *   volta ao login com o aviso de que o pedido está em análise; se falhar, reabre o formulário com a
 *   mensagem e o que foi digitado (nunca as senhas).
 */

import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.service.CadastroService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    private final CadastroService cadastroService;

    public CadastroController(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    @GetMapping
    public String tela() {
        return "servicos/solicitar-cadastro";
    }

    @PostMapping
    public String solicitar(@RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String senha,
                            @RequestParam String confirmarSenha,
                            RedirectAttributes redirect) {
        try {
            cadastroService.solicitar(nome, email, senha, confirmarSenha, Roles.COMPRADOR);
            return "redirect:/login?cadastroEnviado";
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        redirect.addFlashAttribute("formNome", nome);
        redirect.addFlashAttribute("formEmail", email);
        return "redirect:/cadastro";
    }
}
