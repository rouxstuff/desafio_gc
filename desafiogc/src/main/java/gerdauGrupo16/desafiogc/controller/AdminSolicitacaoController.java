package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: AdminSolicitacaoController.java
 * TIPO: controller de telas (@Controller), rota /admin/solicitacoes, restrito ao ADMIN.
 * FUNÇÃO: fila de aprovação. Mostra os serviços PENDENTES pedidos pelos compradores e permite aprovar ou
 *   reprovar cada um.
 * MÉTODOS:
 * - listar(model): carrega os serviços com status PENDENTE e abre a tela.
 * - aprovar(codigo, redirect): muda o serviço para APROVADO (ServicoService.aprovar).
 * - reprovar(codigo, motivo, redirect): muda para REJEITADO; o motivo é obrigatório
 *   (ServicoService.reprovar). Em ambos os casos, se o pedido já foi decidido a mensagem de erro aparece na
 *   tela.
 */

import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.service.ServicoService;
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

@Controller
@RequestMapping("/admin/solicitacoes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSolicitacaoController {

    private final ServicoService service;

    public AdminSolicitacaoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pendentes", service.listarPorStatus(StatusServico.PENDENTE));
        return "admin/solicitacoes";
    }

    @PostMapping("/{codigo}/aprovar")
    public String aprovar(@PathVariable String codigo, RedirectAttributes redirect) {
        try {
            service.aprovar(codigo);
            redirect.addFlashAttribute("sucesso", "Serviço " + codigo + " aprovado.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/admin/solicitacoes";
    }

    @PostMapping("/{codigo}/reprovar")
    public String reprovar(@PathVariable String codigo, @RequestParam String motivo, RedirectAttributes redirect) {
        try {
            service.reprovar(codigo, motivo);
            redirect.addFlashAttribute("sucesso", "Serviço " + codigo + " reprovado.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/admin/solicitacoes";
    }
}
