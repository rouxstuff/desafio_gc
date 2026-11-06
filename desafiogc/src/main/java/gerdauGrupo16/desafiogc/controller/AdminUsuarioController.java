package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: AdminUsuarioController.java
 * TIPO: controller de telas (@Controller), rota /admin/usuarios, restrito ao ADMIN.
 * FUNÇÃO: aba Usuários. O administrador cria usuários (login por e-mail), define o perfil (ADMIN, COMPRADOR
 *   ou FORNECEDOR) e liga cada usuário de perfil FORNECEDOR a um fornecedor existente. A regra de negócio
 *   está em UsuarioService.
 * MÉTODOS:
 * - tela(fornecedorId, model): monta a página com os usuários, os fornecedores e os fornecedores ainda sem
 *   usuário. Se vier fornecedorId (botão Criar usuário da lista), o formulário abre já com o perfil
 *   Fornecedor e esse fornecedor escolhido.
 * - criar(email, senha, perfil, fornecedorId, redirect): cria o usuário; em caso de erro devolve a mensagem
 *   e o que foi digitado (nunca a senha).
 * - vincular(id, fornecedorId, redirect): liga ou troca o fornecedor de um usuário de perfil Fornecedor.
 * - erroNoFormulario(redirect, mensagem, email, perfil, fornecedorId): método auxiliar que guarda o erro e
 *   os valores digitados para reexibir o formulário.
 */

import gerdauGrupo16.desafiogc.model.entity.Usuario;
import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.service.UsuarioService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
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

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String tela(@RequestParam(value = "fornecedorId", required = false) Long fornecedorId, Model model) {
        UsuarioService.Painel painel = usuarioService.painel();
        model.addAttribute("usuarios", painel.usuarios());
        model.addAttribute("fornecedores", painel.fornecedores());
        model.addAttribute("fornecedoresSemUsuario", painel.fornecedoresSemUsuario());
        model.addAttribute("perfis", Roles.values());
        model.addAttribute("perfilFornecedor", Roles.FORNECEDOR);

        if (!model.containsAttribute("formPerfil") && fornecedorId != null) {
            model.addAttribute("formPerfil", Roles.FORNECEDOR.name());
            model.addAttribute("formFornecedorId", fornecedorId);
        }
        return "admin/usuarios";
    }

    @PostMapping
    public String criar(@RequestParam String email,
                        @RequestParam String senha,
                        @RequestParam String perfil,
                        @RequestParam(value = "fornecedorId", required = false) Long fornecedorId,
                        RedirectAttributes redirect) {
        try {
            Roles role = Roles.valueOf(perfil.strip().toUpperCase(Locale.ROOT));
            Usuario criado = usuarioService.criar(email, senha, role, fornecedorId);
            String vinculo = criado.getFornecedor() == null ? ""
                    : " e vinculado ao fornecedor " + criado.getFornecedor().getCodigoFornecedor();
            redirect.addFlashAttribute("sucesso", "Usuário " + criado.getEmail() + " criado" + vinculo + ".");
        } catch (IllegalArgumentException e) {
            erroNoFormulario(redirect, "Perfil inválido.", email, perfil, fornecedorId);
        } catch (ResponseStatusException e) {
            erroNoFormulario(redirect, e.getReason(), email, perfil, fornecedorId);
        } catch (DataIntegrityViolationException e) {

            erroNoFormulario(redirect, "Não foi possível criar: o e-mail ou o fornecedor já está em uso por outro usuário.", email, perfil, fornecedorId);
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/fornecedor")
    public String vincular(@PathVariable Long id,
                           @RequestParam(value = "fornecedorId", required = false) Long fornecedorId,
                           RedirectAttributes redirect) {
        try {
            Usuario u = usuarioService.vincularFornecedor(id, fornecedorId);
            redirect.addFlashAttribute("sucesso", "Usuário " + u.getEmail() + " vinculado ao fornecedor "
                    + u.getFornecedor().getCodigoFornecedor() + ".");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erro", "Este fornecedor já tem um usuário.");
        }
        return "redirect:/admin/usuarios";
    }

    private void erroNoFormulario(RedirectAttributes redirect, String mensagem, String email, String perfil, Long fornecedorId) {
        redirect.addFlashAttribute("erro", mensagem);
        redirect.addFlashAttribute("formEmail", email);
        redirect.addFlashAttribute("formPerfil", perfil);
        redirect.addFlashAttribute("formFornecedorId", fornecedorId);
    }
}
