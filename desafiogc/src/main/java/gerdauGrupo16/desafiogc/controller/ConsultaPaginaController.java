package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.service.CategoriaService;
import gerdauGrupo16.desafiogc.service.ServicoService;
import gerdauGrupo16.desafiogc.service.UnidadeCustoService;
import gerdauGrupo16.desafiogc.service.UnidadeMedidaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ConsultaPaginaController {

    private final ServicoService service;
    private final UnidadeCustoService adicionaisService;
    // [NOVO] usados pelo formulário de cadastro (dropdowns de categoria e unidade de medida)
    private final CategoriaService categoriaService;
    private final UnidadeMedidaService unidadeMedidaService;

    public ConsultaPaginaController(ServicoService service, UnidadeCustoService adicionaisService,
                                    CategoriaService categoriaService, UnidadeMedidaService unidadeMedidaService) {
        this.service = service;
        this.adicionaisService = adicionaisService;
        this.categoriaService = categoriaService;
        this.unidadeMedidaService = unidadeMedidaService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/servicos";
    }

    @GetMapping("/servicos")
    public String consultar(@RequestParam(required = false) String nomeServico, Model model,
                            RedirectAttributes redirect) {
        boolean buscou = nomeServico != null && !nomeServico.isBlank();
        model.addAttribute("nomeServico", nomeServico);

        if (!buscou) {
            model.addAttribute("candidatos", List.of());
            return "servicos/busca";
        }

        List<Servico> candidatos = service.buscarCandidatos(nomeServico);

        if (candidatos.isEmpty()) {
            return paginaNaoEncontrado(service.padronizar(nomeServico), model);
        }
        if (candidatos.size() == 1) {
            // [ALTERADO] a chave agora é o CÓDIGO (o nome pode repetir em categorias diferentes)
            redirect.addAttribute("codigo", candidatos.get(0).getCodigoServico());
            redirect.addAttribute("digitado", nomeServico);
            return "redirect:/servicos/detalhes";
        }
        model.addAttribute("candidatos", candidatos);

        return "servicos/busca";
    }

    @GetMapping("/servicos/detalhes")
    public String detalhes(@RequestParam String codigo,
                           @RequestParam(required = false) String digitado,
                           Model model) {
        Optional<Servico> achado = service.buscarPorCodigo(codigo);
        if (achado.isEmpty()) { // Se alguem mexer na URL com um código que nao existe, mandamos de volta para a busca
            return "redirect:/servicos";
        }
        Servico servico = achado.get();

        model.addAttribute("nomeServico", servico.getNomeServico());
        model.addAttribute("servico", servico);
        model.addAttribute("textoExtra", service.tratarTextoExtra(digitado, servico));
        model.addAttribute("grupos", adicionaisService.listarAdicionaisAgrupados());
        // [NOVO] o contrato também escolhe a unidade de medida (vem preenchida se o serviço já tem uma)
        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        return "servicos/detalhes";
    }

    private String paginaNaoEncontrado(String nome, Model model) {
        model.addAttribute("nomeServico", nome);
        model.addAttribute("nomeNovo", nome);
        model.addAttribute("similares", service.similares(nome));
        return "servicos/nao-encontrado";
    }

    @PostMapping("/servicos/confirmar")
    public String confirmar(@RequestParam String codigo,
                            @RequestParam(name = "adicionais", required = false) List<Long> ids,
                            @RequestParam(name = "unidadeMedidaId", required = false) Long unidadeMedidaId,
                            Model model) {
        Optional<Servico> achado = service.buscarPorCodigo(codigo);
        if (achado.isEmpty()) {
            return "redirect:/servicos";
        }

        List<String> nomes = new ArrayList<>();
        for (UnidadeCusto adicional : adicionaisService.buscarPorIds(ids)) {
            nomes.add(adicional.getMaoDeObra());
        }

        model.addAttribute("nomeServico", "");
        model.addAttribute("titulo", "Serviço confirmado");
        model.addAttribute("servicoNome", achado.get().getNomeServico());
        model.addAttribute("mensagem", "O serviço já existe no catálogo.");
        model.addAttribute("itens", nomes);
        // [NOVO] unidade de medida escolhida (opcional)
        model.addAttribute("medida", unidadeMedidaId == null ? null : unidadeMedidaService.listarTodasUnidadesMedidas().stream()
                .filter(u -> unidadeMedidaId.equals(u.getId()))
                .map(u -> u.getCodigo() + " - " + u.getDescricao())
                .findFirst().orElse(null));
        return "servicos/concluido";
    }

    @GetMapping("/servicos/novo")
    public String novo(@RequestParam(required = false) String nome, Model model) {
        return paginaCadastro(service.padronizar(nome), null, null, null, null, null, null, model);
    }

    /** [ALTERADO] Antes só validava o nome. Agora cadastra de verdade (status PENDENTE) usando as
     *  mesmas regras da API: código/nome/categoria obrigatórios e bloqueio de duplicata pela régua. */
    @PostMapping("/servicos/novo")
    public String solicitar(@RequestParam String nomeServico,
                            @RequestParam(required = false) Long categoriaId,
                            @RequestParam(required = false) String codigoServico,
                            @RequestParam(required = false) String descricaoServico,
                            @RequestParam(required = false) Long unidadeMedidaId,
                            @RequestParam(required = false) Long unidadeCustoId,
                            Model model) {
        String nome = service.padronizar(nomeServico);
        String codigo = codigoServico == null ? "" : codigoServico.trim();

        String erro = null;
        if (nome.isEmpty()) {
            erro = "Digite o nome do serviço.";
        } else if (nome.length() > ServicoService.LIMITE_SAP) {
            erro = "O nome passou de " + ServicoService.LIMITE_SAP + " caracteres (limite do SAP).";
        } else if (categoriaId == null) {
            erro = "Escolha a categoria.";
        } else if (codigo.isEmpty()) {
            erro = "Informe o código do serviço.";
        } else if (codigo.length() > 20) {
            erro = "O código deve ter no máximo 20 caracteres.";
        } else if (descricaoServico != null && descricaoServico.length() > 100) {
            erro = "A descrição deve ter no máximo 100 caracteres.";
        }
        if (erro != null) {
            return paginaCadastro(nome, erro, categoriaId, codigo, descricaoServico, unidadeMedidaId, unidadeCustoId, model);
        }

        try {
            String descricao = (descricaoServico == null || descricaoServico.isBlank()) ? null : descricaoServico.trim();
            Servico criado = service.criarNovo(new ServicoDTO(codigo, nome, descricao, categoriaId, unidadeMedidaId, unidadeCustoId));

            model.addAttribute("nomeServico", "");
            model.addAttribute("titulo", "Serviço solicitado");
            model.addAttribute("servicoNome", criado.getCodigoServico() + " - " + criado.getNomeServico());
            model.addAttribute("mensagem", "Cadastrado com status PENDENTE. Só depois de aprovado ele poderá ser vinculado a fornecedores.");
            // [NOVO] mostra na confirmação a unidade de medida e a de custo (se escolhidas)
            model.addAttribute("medida", criado.getUnidadeMedida() == null ? null
                    : criado.getUnidadeMedida().getCodigo() + " - " + criado.getUnidadeMedida().getDescricao());
            model.addAttribute("itens", criado.getUnidadeCusto() == null ? List.of()
                    : List.of(criado.getUnidadeCusto().getMaoDeObra()));
            return "servicos/concluido";
        } catch (ResponseStatusException e) {
            // 409 duplicado / 404 não encontrado: mostra a mensagem no próprio formulário
            return paginaCadastro(nome, e.getReason(), categoriaId, codigo, descricaoServico, unidadeMedidaId, unidadeCustoId, model);
        }
    }

    private String paginaCadastro(String nome, String erro, Long categoriaId, String codigo,
                                  String descricao, Long unidadeMedidaId, Long unidadeCustoId, Model model) {
        int n = nome.length();
        model.addAttribute("nomeNovo", nome);
        model.addAttribute("tamanho", n);
        model.addAttribute("limite", ServicoService.LIMITE_SAP);
        model.addAttribute("porcentagem", Math.min(100, n * 100 / ServicoService.LIMITE_SAP));
        model.addAttribute("estourou", n > ServicoService.LIMITE_SAP);
        model.addAttribute("similares", service.similares(nome));
        model.addAttribute("erro", erro);
        // [NOVO] dropdowns e valores já digitados (para não perder o que o usuário preencheu ao dar erro)
        model.addAttribute("categorias", categoriaService.listarTodasCategorias());
        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("codigoServico", codigo == null ? "" : codigo);
        model.addAttribute("descricaoServico", descricao == null ? "" : descricao);
        model.addAttribute("unidadeMedidaId", unidadeMedidaId);
        // [NOVO] unidade de custo (opcional), igual à de medida
        model.addAttribute("unidadesCusto", adicionaisService.listarTodasUnidadesCustos());
        model.addAttribute("unidadeCustoId", unidadeCustoId);
        return "servicos/cadastro";
    }
}
