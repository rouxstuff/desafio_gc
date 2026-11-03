package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.service.FornecedorService;
import gerdauGrupo16.desafiogc.service.FornecedorServicoService;
import gerdauGrupo16.desafiogc.service.ServicoService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * [NOVO] Duas telas extras no estilo da Fernanda:
 *  - /catalogo : todos os serviços com o status (PENDENTE / APROVADO / REJEITADO)
 *  - /vinculos : liga fornecedores a serviços APROVADOS (um serviço pode ter vários fornecedores)
 * Sem regra de negócio aqui: tudo vem dos services (a regra "só APROVADO" está no FornecedorService).
 */
@Controller
@AllArgsConstructor
public class CatalogoPaginaController {

    private final ServicoService servicoService;
    private final FornecedorService fornecedorService;
    private final FornecedorServicoService fornecedorServicoService;

    private static final int TAMANHO_PAGINA = 50;

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(value = "q", required = false) String q,
                           @RequestParam(value = "pagina", defaultValue = "0") int pagina, Model model) {
        Page<Servico> resultado = servicoService.listarPagina(q, pagina, TAMANHO_PAGINA);
        model.addAttribute("servicos", resultado.getContent());
        model.addAttribute("q", q == null || q.isBlank() ? "" : q);
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        model.addAttribute("total", resultado.getTotalElements());
        return "servicos/catalogo";
    }

    @GetMapping("/vinculos")
    public String vinculos(@RequestParam(value = "servicoFiltro", required = false) String servicoFiltro,
                           Model model) {
        model.addAttribute("fornecedores", montarFornecedores());
        model.addAttribute("servicosVinculaveis", servicoService.listarOpcoesPorStatus(StatusServico.APROVADO));

        // novo: só busca se um serviço foi escolhido
        if (servicoFiltro != null && !servicoFiltro.isBlank()) {
            List<String> vinculados = fornecedorServicoService.buscarFornecedoresDoServico(servicoFiltro).stream()
                    .map(this::rotulo)   // reaproveita o seu método rotulo()
                    .toList();
            model.addAttribute("fornecedoresVinculados", vinculados);
        }
        model.addAttribute("servicoFiltro", servicoFiltro);

        return "servicos/vinculos";
    }

    @PostMapping("/vinculos")
    public String vincular(@RequestParam(value = "fornecedorId", required = false) Long fornecedorId,
                           @RequestParam(value = "codigoServico", required = false) String codigoServico,
                           RedirectAttributes redirect) {
        if (fornecedorId == null || codigoServico == null || codigoServico.isBlank()) {
            redirect.addFlashAttribute("erro", "Escolha o fornecedor e o serviço para vincular.");
            return "redirect:/vinculos";
        }
        try {
            fornecedorService.vincularServico(fornecedorId, codigoServico);
            redirect.addFlashAttribute("sucesso", "Fornecedor vinculado ao serviço " + codigoServico + ".");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/vinculos";
    }


    private List<LinhaFornecedor> montarFornecedores() {
        return fornecedorService.buscarTodos().stream()
                .sorted(Comparator.comparing(Fornecedor::getId))
                // [PERFORMANCE] antes: 1 consulta por fornecedor (e 1 por vínculo) só para montar uma lista que a tela não usa
                .map(f -> new LinhaFornecedor(f.getId(), rotulo(f)))
                .toList();
    }

    /** O código do fornecedor no seed é um UUID enorme; na tela mostramos só o começo. */
    private String rotulo(Fornecedor f) {
        if (f.getNomeFornecedor() != null && !f.getNomeFornecedor().isBlank()) {
            return "Fornecedor " + f.getId() + " - " + f.getNomeFornecedor();
        }
        String codigo = f.getCodigoFornecedor() == null ? "" : f.getCodigoFornecedor();
        String curto = codigo.length() > 8 ? codigo.substring(0, 8) : codigo;
        return "Fornecedor " + f.getId() + (curto.isEmpty() ? "" : " (" + curto + ")");
    }

    @Getter
    @AllArgsConstructor
    public static class LinhaFornecedor {
        private final Long id;
        private final String rotulo;
    }
}
