package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: AdminUsuarioController.java
 * TIPO: controller de telas (@Controller), rota /admin/usuarios, restrito ao ADMIN.
 * FUNÇÃO: aba Usuários. O administrador cria usuários (login por e-mail), define o perfil (ADMIN ou
 *   COMPRADOR) e liga, se quiser, cada usuário Comprador a um comprador da base. A regra de negócio
 *   está em UsuarioService. A aba também mostra a fila de solicitações de cadastro (CadastroService) para
 *   aprovar ou reprovar.
 * MÉTODOS:
 * - tela(compradorId, model): monta a página com os usuários, os compradores e os compradores ainda sem
 *   usuário. Se vier compradorId (botão Criar usuário da lista), o formulário abre já com o perfil
 *   Comprador e esse comprador escolhido.
 * - aprovarCadastro(id, perfil, compradorId, auth, redirect): aprova uma solicitação de cadastro feita na
 *   tela pública; cria o usuário com a senha que a pessoa escolheu. O administrador pode ligar o usuário
 *   a um comprador da base (opcional).
 * - reprovarCadastro(id, motivo, auth, redirect): recusa a solicitação; o motivo é obrigatório.
 * - criar(email, senha, perfil, compradorId, redirect): cria o usuário; em caso de erro devolve a mensagem
 *   e o que foi digitado (nunca a senha).
 * - vincular(id, compradorId, redirect): liga ou troca o comprador de um usuário de perfil Comprador.
 * - erroNoFormulario(redirect, mensagem, email, perfil, compradorId): método auxiliar que guarda o erro e
 *   os valores digitados para reexibir o formulário.
 */

import gerdauGrupo16.desafiogc.model.entity.Usuario;
import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.service.CadastroService;
import gerdauGrupo16.desafiogc.service.UsuarioService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final CadastroService cadastroService;

    public AdminUsuarioController(UsuarioService usuarioService, CadastroService cadastroService) {
        this.usuarioService = usuarioService;
        this.cadastroService = cadastroService;
    }

    @GetMapping
    public String tela(@RequestParam(value = "compradorId", required = false) Long compradorId, Model model) {
        UsuarioService.Painel painel = usuarioService.painel();
        model.addAttribute("usuarios", painel.usuarios());
        model.addAttribute("compradores", painel.compradores());
        model.addAttribute("compradoresSemUsuario", painel.compradoresSemUsuario());
        model.addAttribute("perfis", Roles.values());
        model.addAttribute("solicitacoes", cadastroService.listarPendentes());
        model.addAttribute("perfilComprador", Roles.COMPRADOR);

        if (!model.containsAttribute("formPerfil") && compradorId != null) {
            model.addAttribute("formPerfil", Roles.COMPRADOR.name());
            model.addAttribute("formCompradorId", compradorId);
        }
        return "admin/usuarios";
    }

    @PostMapping("/solicitacoes/{id}/aprovar")
    public String aprovarCadastro(@PathVariable Long id,
                                  @RequestParam(value = "perfil", required = false) String perfil,
                                  @RequestParam(value = "compradorId", required = false) Long compradorId,
                                  Authentication auth,
                                  RedirectAttributes redirect) {
        try {
            Roles role = perfil == null || perfil.isBlank() ? null : Roles.valueOf(perfil.strip().toUpperCase(Locale.ROOT));
            var aprovada = cadastroService.aprovar(id, role, compradorId, auth.getName());
            redirect.addFlashAttribute("sucesso", "Cadastro de " + aprovada.getEmail() + " aprovado. O usuário já pode entrar.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", "Perfil inválido.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erro", "Não foi possível aprovar: o e-mail ou o comprador já está em uso por outro usuário.");
        }
        return "redirect:/admin/usuarios#solicitacoes";
    }

    @PostMapping("/solicitacoes/{id}/reprovar")
    public String reprovarCadastro(@PathVariable Long id,
                                   @RequestParam String motivo,
                                   Authentication auth,
                                   RedirectAttributes redirect) {
        try {
            var reprovada = cadastroService.reprovar(id, motivo, auth.getName());
            redirect.addFlashAttribute("sucesso", "Cadastro de " + reprovada.getEmail() + " reprovado.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/admin/usuarios#solicitacoes";
    }

    @PostMapping
    public String criar(@RequestParam String email,
                        @RequestParam String senha,
                        @RequestParam String perfil,
                        @RequestParam(value = "compradorId", required = false) Long compradorId,
                        RedirectAttributes redirect) {
        try {
            Roles role = Roles.valueOf(perfil.strip().toUpperCase(Locale.ROOT));
            Usuario criado = usuarioService.criar(email, senha, role, compradorId);
            String vinculo = criado.getComprador() == null ? ""
                    : " e vinculado ao comprador " + criado.getComprador().getCodigoComprador();
            redirect.addFlashAttribute("sucesso", "Usuário " + criado.getEmail() + " criado" + vinculo + ".");
        } catch (IllegalArgumentException e) {
            erroNoFormulario(redirect, "Perfil inválido.", email, perfil, compradorId);
        } catch (ResponseStatusException e) {
            erroNoFormulario(redirect, e.getReason(), email, perfil, compradorId);
        } catch (DataIntegrityViolationException e) {

            erroNoFormulario(redirect, "Não foi possível criar: o e-mail ou o comprador já está em uso por outro usuário.", email, perfil, compradorId);
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/comprador")
    public String vincular(@PathVariable Long id,
                           @RequestParam(value = "compradorId", required = false) Long compradorId,
                           RedirectAttributes redirect) {
        try {
            Usuario u = usuarioService.vincularComprador(id, compradorId);
            redirect.addFlashAttribute("sucesso", "Usuário " + u.getEmail() + " vinculado ao comprador "
                    + u.getComprador().getCodigoComprador() + ".");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erro", "Este comprador já tem um usuário.");
        }
        return "redirect:/admin/usuarios";
    }

    private void erroNoFormulario(RedirectAttributes redirect, String mensagem, String email, String perfil, Long compradorId) {
        redirect.addFlashAttribute("erro", mensagem);
        redirect.addFlashAttribute("formEmail", email);
        redirect.addFlashAttribute("formPerfil", perfil);
        redirect.addFlashAttribute("formCompradorId", compradorId);
    }
}
