package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.controller.dto.ServicoForm;
import gerdauGrupo16.desafiogc.controller.dto.SugestaoDTO;
import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.service.CategoriaService;
import gerdauGrupo16.desafiogc.service.FornecedorService;
import gerdauGrupo16.desafiogc.service.ServicoService;
import gerdauGrupo16.desafiogc.service.UnidadeCustoService;
import gerdauGrupo16.desafiogc.service.UnidadeMedidaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * [NOVO] Tela web (Thymeleaf) para apresentar o MVP.
 *
 * É um @Controller (devolve o NOME de uma página, templates/index.html), diferente dos
 * @RestController do projeto (que devolvem JSON). Não tem regra de negócio: tudo é delegado
 * aos mesmos services que a API REST usa.
 */
@Controller
@AllArgsConstructor
public class PaginaController {

    // Mesmo valor do LIMITE_BLOQUEIO do ServicoService (0.90), em percentual.
    // Se mudar lá, mude aqui.
    private static final double BLOQUEIA_A_PARTIR_DE = 90.0;

    private final ServicoService servicoService;
    private final FornecedorService fornecedorService;
    private final CategoriaService categoriaService;
    private final UnidadeMedidaService unidadeMedidaService;
    private final UnidadeCustoService unidadeCustoService;

    /** Tela inicial. ?q=texto filtra o catálogo pelo nome. */
    @GetMapping("/")
    public String home(@RequestParam(value = "q", required = false) String q, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ServicoForm());
        }
        carregarTela(model, q);
        return "index";
    }

    /** Botão "Verificar semelhança": consulta a régua e mostra os parecidos. Não grava nada. */
    @PostMapping("/web/verificar")
    public String verificar(@ModelAttribute("form") ServicoForm form, Model model) {
        if (form.getCategoriaId() == null || form.getNomeServico() == null || form.getNomeServico().isBlank()) {
            model.addAttribute("erro", "Para verificar, escolha a categoria e digite o nome do serviço.");
        } else {
            List<SugestaoDTO> sugestoes = servicoService.buscarSugestoes(form.getCategoriaId(), form.getNomeServico());
            model.addAttribute("sugestoes", sugestoes);
            model.addAttribute("statusVerificacao", statusDe(sugestoes));
        }
        carregarTela(model, null);
        return "index";
    }

    /** Botão "Cadastrar": valida e chama o mesmo criarNovo da API. Erros de negócio viram mensagem na tela. */
    @PostMapping("/web/cadastrar")
    public String cadastrar(@Valid @ModelAttribute("form") ServicoForm form, BindingResult resultado,
                            Model model, RedirectAttributes redirect) {
        // Com RedirectAttributes na assinatura, o Spring não repassa "form" nem os erros para a view
        // quando NÃO há redirect. Por isso recolocamos no model à mão.
        model.addAttribute("form", form);
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "form", resultado);
        if (resultado.hasErrors()) {
            carregarTela(model, null);
            return "index";
        }
        try {
            Servico criado = servicoService.criarNovo(form.paraDto());
            redirect.addFlashAttribute("sucesso", "Serviço " + criado.getCodigoServico() + " - "
                    + criado.getNomeServico() + " cadastrado com status PENDENTE.");
            return "redirect:/";
        } catch (ResponseStatusException e) {
            model.addAttribute("erro", e.getReason());
            carregarTela(model, null);
            return "index";
        }
    }

    /** Vincula um fornecedor a um serviço que já existe (tabela FornecedorServico). */
    @PostMapping("/web/vincular")
    public String vincular(@RequestParam(value = "fornecedorId", required = false) Long fornecedorId,
                           @RequestParam(value = "codigoServico", required = false) String codigoServico,
                           RedirectAttributes redirect) {
        if (fornecedorId == null || codigoServico == null || codigoServico.isBlank()) {
            redirect.addFlashAttribute("erro", "Escolha o fornecedor e o serviço para vincular.");
            return "redirect:/";
        }
        try {
            fornecedorService.vincularServico(fornecedorId, codigoServico);
            redirect.addFlashAttribute("sucesso", "Fornecedor vinculado ao serviço " + codigoServico + ".");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/";
    }

    // ---------------------------------------------------------------- auxiliares

    private String statusDe(List<SugestaoDTO> sugestoes) {
        if (sugestoes.isEmpty()) return "LIVRE";
        return sugestoes.get(0).aderenciaPercentual() >= BLOQUEIA_A_PARTIR_DE ? "DUPLICADO" : "PARECIDOS";
    }

    /** Coloca no model tudo que a página precisa (listas dos dropdowns, catálogo e vínculos). */
    private void carregarTela(Model model, String q) {
        model.addAttribute("categorias", categoriaService.listarTodasCategorias());
        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        model.addAttribute("unidadesCusto", unidadeCustoService.listarTodasUnidadesCustos());

        List<Servico> todos = servicoService.listarTodosServicos();
        boolean semFiltro = q == null || q.isBlank();
        model.addAttribute("todosServicos", todos);
        // [NOVO] o dropdown de vínculo só mostra serviços APROVADOS (o bloqueio de verdade está no FornecedorService)
        model.addAttribute("servicosVinculaveis", todos.stream()
                .filter(s -> s.getStatus() == StatusServico.APROVADO).toList());
        model.addAttribute("servicos", semFiltro ? todos : servicoService.buscarPorNome(q));
        model.addAttribute("q", semFiltro ? "" : q);

        model.addAttribute("fornecedores", montarFornecedores());
    }

    private List<LinhaFornecedor> montarFornecedores() {
        return fornecedorService.listarTodosFornecedores().stream()
                .map(f -> new LinhaFornecedor(
                        f.getId(),
                        rotulo(f),
                        fornecedorService.listarServicosDoFornecedor(f.getId()).stream()
                                .map(s -> s.getCodigoServico() + " - " + s.getNomeServico())
                                .toList()))
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

    /** Linha da tabela de vínculos e item do dropdown de fornecedores. */
    @Getter
    @AllArgsConstructor
    public static class LinhaFornecedor {
        private final Long id;
        private final String rotulo;
        private final List<String> servicos;
    }
}
