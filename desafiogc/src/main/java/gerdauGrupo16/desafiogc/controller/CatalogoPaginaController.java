package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: CatalogoPaginaController.java
 * TIPO: controller de telas (@Controller).
 * FUNÇÃO: duas telas. (1) /catalogo: lista de todos os serviços com status, para comprador e fornecedor.
 *   (2) /vinculos: o administrador liga fornecedores a serviços APROVADOS e consulta quem atende cada
 *   serviço. Não tem regra de negócio: tudo vem dos services.
 * CLASSE INTERNA: LinhaFornecedor (id e rótulo) é o item da lista suspensa de fornecedores.
 * MÉTODOS:
 * - catalogo(q, pagina, auth, model): catálogo paginado (50 por página) com busca por nome ou código, sem
 *   os serviços rejeitados. O administrador é redirecionado para /admin/servicos.
 * - vinculos(servicoFiltro, busca, model): abre a tela de vínculos. Tem barra de pesquisa por nome ou
 *   código (até 50 resultados, só serviços aprovados); se a busca achar um serviço só, ele já abre. Mostra
 *   os fornecedores do serviço escolhido.
 * - vincular(fornecedorId, codigoServico, redirect): cria o vínculo (FornecedorService.vincularServico) e
 *   volta para a tela com sucesso ou erro.
 * - montarFornecedores(): monta a lista de fornecedores (id e rótulo) ordenada por id.
 * - rotulo(f): texto do fornecedor na tela: Fornecedor ID - nome, ou Fornecedor ID (8 primeiros caracteres
 *   do código) quando não há nome.
 */

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.repository.ServicoOpcaoView;
import gerdauGrupo16.desafiogc.service.FornecedorService;
import gerdauGrupo16.desafiogc.service.FornecedorServicoService;
import gerdauGrupo16.desafiogc.service.ServicoService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
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

@Controller
@AllArgsConstructor
public class CatalogoPaginaController {

    private final ServicoService servicoService;
    private final FornecedorService fornecedorService;
    private final FornecedorServicoService fornecedorServicoService;

    private static final int TAMANHO_PAGINA = 50;
    private static final int MAX_RESULTADOS_BUSCA = 50;

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(value = "q", required = false) String q,
                           @RequestParam(value = "pagina", defaultValue = "0") int pagina,
                           Authentication auth, Model model) {

        if (PerfilAcesso.ehAdmin(auth)) {
            return "redirect:/admin/servicos" + (q == null || q.isBlank() ? "" : "?q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
        }
        Page<Servico> resultado = servicoService.listarPagina(q, pagina, TAMANHO_PAGINA, false);

        model.addAttribute("servicos", resultado.getContent().stream()
                .filter(s -> ServicoService.visivelPara(s, false)).toList());
        model.addAttribute("q", q == null || q.isBlank() ? "" : q);
        model.addAttribute("paginaAtual", resultado.getNumber());
        model.addAttribute("totalPaginas", resultado.getTotalPages());
        model.addAttribute("total", resultado.getTotalElements());
        return "servicos/catalogo";
    }

    @GetMapping("/vinculos")
    public String vinculos(@RequestParam(value = "servicoFiltro", required = false) String servicoFiltro,
                           @RequestParam(value = "busca", required = false) String busca,
                           Model model) {
        model.addAttribute("fornecedores", montarFornecedores());
        model.addAttribute("servicosVinculaveis", servicoService.listarOpcoesPorStatus(StatusServico.APROVADO));

        boolean buscou = busca != null && !busca.isBlank();
        model.addAttribute("busca", buscou ? busca.strip() : "");
        if (buscou) {
            List<ServicoOpcaoView> achados = servicoService.buscarOpcoes(StatusServico.APROVADO, busca, MAX_RESULTADOS_BUSCA);
            model.addAttribute("resultadosBusca", achados);

            if ((servicoFiltro == null || servicoFiltro.isBlank()) && achados.size() == 1) {
                servicoFiltro = achados.get(0).getCodigoServico();
            }
        }

        if (servicoFiltro != null && !servicoFiltro.isBlank()) {
            servicoService.buscarPorCodigo(servicoFiltro).ifPresent(s ->
                    model.addAttribute("servicoSelecionado", s.getCodigoServico() + " - " + s.getNomeServico()));
            List<String> vinculados = fornecedorServicoService.buscarFornecedoresDoServico(servicoFiltro).stream()
                    .map(this::rotulo)
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

                .map(f -> new LinhaFornecedor(f.getId(), rotulo(f)))
                .toList();
    }

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
