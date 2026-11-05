package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.ServicoService;
import gerdauGrupo16.desafiogc.service.UnidadeCustoService;
import gerdauGrupo16.desafiogc.service.UnidadeMedidaService;
import org.springframework.data.domain.Page;
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

/**
 * Aba "Serviços" do administrador: localiza um serviço e altera as características dele (unidade de medida,
 * unidade de custo e descrição). Também é para onde o administrador vai ao clicar num serviço do Catálogo,
 * já que o administrador não contrata serviço. Duas travas: regra de URL (SecurityConfiguration) e @PreAuthorize.
 */
@Controller
@RequestMapping("/admin/servicos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminServicoController {

    private static final int TAMANHO_PAGINA = 50;

    private final ServicoService servicoService;
    private final UnidadeMedidaService unidadeMedidaService;
    private final UnidadeCustoService unidadeCustoService;

    public AdminServicoController(ServicoService servicoService, UnidadeMedidaService unidadeMedidaService,
                                  UnidadeCustoService unidadeCustoService) {
        this.servicoService = servicoService;
        this.unidadeMedidaService = unidadeMedidaService;
        this.unidadeCustoService = unidadeCustoService;
    }

    /** Lista com busca por nome ou código; cada linha leva para a tela de alteração. */
    @GetMapping
    public String lista(@RequestParam(value = "q", required = false) String q,
                        @RequestParam(value = "pagina", defaultValue = "0") int pagina, Model model) {
        Page<Servico> resultado = servicoService.listarPagina(q, pagina, TAMANHO_PAGINA, true);   // o administrador vê todos, inclusive rejeitados
        model.addAttribute("servicos", resultado.getContent());
        model.addAttribute("q", q == null || q.isBlank() ? "" : q);
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        model.addAttribute("total", resultado.getTotalElements());
        return "admin/servicos";
    }

    @GetMapping("/{codigo}")
    public String editar(@PathVariable String codigo, Model model, RedirectAttributes redirect) {
        return servicoService.buscarPorCodigo(codigo).map(servico -> {
            model.addAttribute("servico", servico);
            model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
            model.addAttribute("unidadesCusto", unidadeCustoService.listarTodasUnidadesCustos());
            model.addAttribute("maxDescricao", ServicoService.LIMITE_DESCRICAO);
            return "admin/servico-editar";
        }).orElseGet(() -> {
            redirect.addFlashAttribute("erro", "Serviço não encontrado.");
            return "redirect:/admin/servicos";
        });
    }

    @PostMapping("/{codigo}")
    public String salvar(@PathVariable String codigo,
                         @RequestParam(value = "unidadeMedidaId", required = false) Long unidadeMedidaId,
                         @RequestParam(value = "unidadeCustoId", required = false) Long unidadeCustoId,
                         @RequestParam(value = "descricaoServico", required = false) String descricaoServico,
                         RedirectAttributes redirect) {
        try {
            Servico s = servicoService.atualizarCaracteristicas(codigo, unidadeMedidaId, unidadeCustoId, descricaoServico);
            redirect.addFlashAttribute("sucesso", "Características do serviço " + s.getCodigoServico() + " salvas.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/admin/servicos/" + codigo;
    }
}
