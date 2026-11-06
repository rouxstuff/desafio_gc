package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: ConsultaPaginaController.java
 * TIPO: controller de telas (@Controller). É o fluxo principal de comprador e fornecedor.
 * FUNÇÃO: consulta de serviços, confirmação de um serviço existente e solicitação de serviço novo. Regra
 *   geral: procure primeiro, crie só se não existir. O administrador não contrata nem solicita; quando cai
 *   aqui é redirecionado para /admin/servicos.
 * MÉTODOS:
 * - raiz(): a rota / redireciona para /servicos.
 * - consultar(nomeServico, auth, model, redirect): tela de busca. Aceita vários termos separados por ponto
 *   e vírgula (lista combinada). Com um termo: nenhum resultado abre Serviço não encontrado; um resultado
 *   abre direto os detalhes; vários mostram a lista.
 * - detalhes(codigo, digitado, auth, model): tela de detalhes e confirmação do serviço. Serviço rejeitado
 *   não abre para quem não é administrador.
 * - ehAdmin(auth): atalho para PerfilAcesso.ehAdmin.
 * - modoContrato(servico, admin): verdadeiro quando o serviço está APROVADO e o usuário não é admin; nesse
 *   modo a unidade de medida vem do catálogo e não pode ser escolhida.
 * - paginaDetalhes(servico, digitado, erro, quantidade, observacao, unidadeMedidaId, admin, model):
 *   preenche o model da tela de detalhes (adicionais, unidades, dica de quantidade, valores já digitados).
 * - buscarUnidade(id): acha a unidade de medida pelo id (ou null).
 * - paginaNaoEncontrado(nome, model): tela Serviço não encontrado, com as sugestões parecidas.
 * - confirmar(codigo, ids, unidadeMedidaId, quantidade, observacao, digitado, auth, model): valida a
 *   quantidade conforme a unidade e a observação e mostra o resumo na tela de conclusão. Não grava
 *   contratação.
 * - novo(nome, model): abre o formulário de novo serviço com o nome já padronizado.
 * - solicitar(nomeServico, categoriaId, descricaoServico, unidadeMedidaId, unidadeCustoId, quantidade,
 *   observacao, principal, model): valida os campos e cria o serviço PENDENTE (ServicoService.criarNovo).
 *   Erro de duplicado ou limite aparece no próprio formulário.
 * - paginaCadastro(nome, erro, categoriaId, descricao, unidadeMedidaId, unidadeCustoId, quantidade,
 *   observacao, model): preenche o model do formulário (contador de caracteres, sugestões, listas e valores
 *   já digitados).
 */

import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.service.QuantidadeServico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.service.CategoriaService;
import gerdauGrupo16.desafiogc.service.ServicoService;
import gerdauGrupo16.desafiogc.service.TermosBusca;
import gerdauGrupo16.desafiogc.service.UnidadeCustoService;
import gerdauGrupo16.desafiogc.service.UnidadeMedidaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ConsultaPaginaController {

    private final ServicoService service;
    private final UnidadeCustoService adicionaisService;

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
    public String consultar(@RequestParam(required = false) String nomeServico, Authentication auth, Model model,
                            RedirectAttributes redirect) {
        boolean admin = ehAdmin(auth);
        boolean buscou = nomeServico != null && !nomeServico.isBlank();
        model.addAttribute("nomeServico", nomeServico);
        model.addAttribute("multiplo", false);

        List<String> termos = TermosBusca.separar(nomeServico);

        if (!buscou || termos.isEmpty()) {
            model.addAttribute("candidatos", List.of());
            return "servicos/busca";
        }

        if (termos.size() > 1) {
            ServicoService.BuscaMultipla busca = service.buscarMultiplo(nomeServico, admin);
            model.addAttribute("multiplo", true);
            model.addAttribute("totalTermos", busca.termos().size());
            model.addAttribute("candidatos", busca.candidatos());
            model.addAttribute("termosPorCodigo", busca.termosPorCodigo());
            model.addAttribute("termosSemResultado", busca.semResultado());
            return "servicos/busca";
        }

        String termo = termos.get(0);
        model.addAttribute("nomeServico", termo);
        List<Servico> candidatos = service.buscarCandidatos(termo, admin);

        if (candidatos.isEmpty()) {
            return paginaNaoEncontrado(service.padronizar(termo), model);
        }
        if (candidatos.size() == 1) {

            redirect.addAttribute("codigo", candidatos.get(0).getCodigoServico());
            redirect.addAttribute("digitado", termo);
            return "redirect:/servicos/detalhes";
        }
        model.addAttribute("candidatos", candidatos);

        return "servicos/busca";
    }

    @GetMapping("/servicos/detalhes")
    public String detalhes(@RequestParam String codigo,
                           @RequestParam(required = false) String digitado,
                           Authentication auth,
                           Model model) {

        if (ehAdmin(auth)) return "redirect:/admin/servicos/" + codigo;
        Optional<Servico> achado = service.buscarPorCodigo(codigo)
                .filter(s -> ServicoService.visivelPara(s, false));
        if (achado.isEmpty()) {
            return "redirect:/servicos";
        }
        return paginaDetalhes(achado.get(), digitado, null, null, null, null, ehAdmin(auth), model);
    }

    private static boolean ehAdmin(Authentication auth) {
        return PerfilAcesso.ehAdmin(auth);
    }

    private boolean modoContrato(Servico servico, boolean admin) {
        return servico.getStatus() == StatusServico.APROVADO && !admin;
    }

    private String paginaDetalhes(Servico servico, String digitado, String erro, String quantidade,
                                  String observacao, Long unidadeMedidaId, boolean admin, Model model) {
        boolean contrato = modoContrato(servico, admin);
        boolean unidadeDefinida = servico.getUnidadeMedida() != null;
        model.addAttribute("nomeServico", servico.getNomeServico());
        model.addAttribute("servico", servico);
        model.addAttribute("digitado", digitado == null ? "" : digitado);
        model.addAttribute("textoExtra", service.tratarTextoExtra(digitado, servico));
        model.addAttribute("grupos", adicionaisService.listarAdicionaisAgrupados());

        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        model.addAttribute("modoContrato", contrato);
        model.addAttribute("quantidadeObrigatoria", contrato && unidadeDefinida);
        model.addAttribute("dicaQuantidade", contrato && unidadeDefinida ? QuantidadeServico.dica(servico.getUnidadeMedida().getCodigo()) : "");
        model.addAttribute("erro", erro);
        model.addAttribute("quantidade", quantidade == null ? "" : quantidade);
        model.addAttribute("observacao", observacao == null ? "" : observacao);
        model.addAttribute("unidadeMedidaEscolhida", unidadeMedidaId);
        model.addAttribute("maxObservacao", QuantidadeServico.MAX_OBSERVACAO);
        return "servicos/detalhes";
    }

    private UnidadeMedida buscarUnidade(Long id) {
        if (id == null) return null;
        return unidadeMedidaService.listarTodasUnidadesMedidas().stream()
                .filter(u -> id.equals(u.getId())).findFirst().orElse(null);
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
                            @RequestParam(name = "quantidade", required = false) String quantidade,
                            @RequestParam(name = "observacao", required = false) String observacao,
                            @RequestParam(name = "digitado", required = false) String digitado,
                            Authentication auth,
                            Model model) {
        if (ehAdmin(auth)) return "redirect:/admin/servicos/" + codigo;
        Optional<Servico> achado = service.buscarPorCodigo(codigo)
                .filter(s -> ServicoService.visivelPara(s, false));
        if (achado.isEmpty()) {
            return "redirect:/servicos";
        }
        Servico servico = achado.get();
        boolean admin = false;

        boolean contrato = modoContrato(servico, admin);
        UnidadeMedida unidade = contrato ? servico.getUnidadeMedida() : buscarUnidade(unidadeMedidaId);

        QuantidadeServico.Resultado qtd = contrato && unidade == null
                ? new QuantidadeServico.Resultado(null, null)
                : QuantidadeServico.validar(quantidade, unidade == null ? null : unidade.getCodigo(), contrato);
        String obs = observacao == null ? "" : observacao.strip();
        String erro = qtd.erro();
        if (erro == null && obs.length() > QuantidadeServico.MAX_OBSERVACAO) {
            erro = "A observação deve ter no máximo " + QuantidadeServico.MAX_OBSERVACAO + " caracteres.";
        }
        if (erro != null) {
            return paginaDetalhes(servico, digitado, erro, quantidade, observacao, unidadeMedidaId, admin, model);
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

        model.addAttribute("medida", unidade == null ? null : unidade.getCodigo() + " - " + unidade.getDescricao());
        model.addAttribute("quantidade", qtd.valor() == null ? null
                : QuantidadeServico.formatar(qtd.valor()) + " " + unidade.getCodigo());
        model.addAttribute("observacao", obs.isEmpty() ? null : obs);
        return "servicos/concluido";
    }

    @GetMapping("/servicos/novo")
    public String novo(@RequestParam(required = false) String nome, Model model) {
        return paginaCadastro(service.padronizar(nome), null, null, null, null, null, null, null, model);
    }

    @PostMapping("/servicos/novo")
    public String solicitar(@RequestParam String nomeServico,
                            @RequestParam(required = false) Long categoriaId,
                            @RequestParam(required = false) String descricaoServico,
                            @RequestParam(required = false) Long unidadeMedidaId,
                            @RequestParam(required = false) Long unidadeCustoId,
                            @RequestParam(required = false) String quantidade,
                            @RequestParam(required = false) String observacao,
                            Principal principal,
                            Model model) {
        String nome = service.padronizar(nomeServico);

        String erro = null;
        if (nome.isEmpty()) {
            erro = "Digite o nome do serviço.";
        } else if (nome.length() > ServicoService.LIMITE_SAP) {
            erro = "O nome passou de " + ServicoService.LIMITE_SAP + " caracteres (limite do SAP).";
        } else if (categoriaId == null) {
            erro = "Escolha a categoria.";
        } else if (descricaoServico != null && descricaoServico.length() > 100) {
            erro = "A descrição deve ter no máximo 100 caracteres.";
        }

        UnidadeMedida unidadeEscolhida = buscarUnidade(unidadeMedidaId);
        QuantidadeServico.Resultado qtd = null;
        String obs = observacao == null ? "" : observacao.strip();
        if (erro == null) {
            qtd = QuantidadeServico.validar(quantidade, unidadeEscolhida == null ? null : unidadeEscolhida.getCodigo(), false);
            erro = qtd.erro();
        }
        if (erro == null && obs.length() > QuantidadeServico.MAX_OBSERVACAO) {
            erro = "A observação deve ter no máximo " + QuantidadeServico.MAX_OBSERVACAO + " caracteres.";
        }
        if (erro != null) {
            return paginaCadastro(nome, erro, categoriaId, descricaoServico, unidadeMedidaId, unidadeCustoId, quantidade, observacao, model);
        }

        try {
            String descricao = (descricaoServico == null || descricaoServico.isBlank()) ? null : descricaoServico.trim();
            Servico criado = service.criarNovo(
                    new ServicoDTO(nome, descricao, categoriaId, unidadeMedidaId, unidadeCustoId),
                    principal.getName());

            model.addAttribute("nomeServico", "");
            model.addAttribute("titulo", "Serviço solicitado");
            model.addAttribute("servicoNome", criado.getCodigoServico() + " - " + criado.getNomeServico());
            model.addAttribute("mensagem", "Cadastrado com status PENDENTE. Só depois de aprovado pelo administrador ele poderá ser vinculado a fornecedores.");
            model.addAttribute("medida", criado.getUnidadeMedida() == null ? null
                    : criado.getUnidadeMedida().getCodigo() + " - " + criado.getUnidadeMedida().getDescricao());
            model.addAttribute("itens", criado.getUnidadeCusto() == null ? List.of()
                    : List.of(criado.getUnidadeCusto().getMaoDeObra()));
            model.addAttribute("quantidade", qtd == null || qtd.valor() == null ? null
                    : QuantidadeServico.formatar(qtd.valor()) + " " + unidadeEscolhida.getCodigo());
            model.addAttribute("observacao", obs.isEmpty() ? null : obs);
            return "servicos/concluido";
        } catch (ResponseStatusException e) {

            return paginaCadastro(nome, e.getReason(), categoriaId, descricaoServico, unidadeMedidaId, unidadeCustoId, quantidade, observacao, model);
        }
    }

    private String paginaCadastro(String nome, String erro, Long categoriaId,
                                  String descricao, Long unidadeMedidaId, Long unidadeCustoId,
                                  String quantidade, String observacao, Model model) {
        int n = nome.length();
        model.addAttribute("nomeNovo", nome);
        model.addAttribute("tamanho", n);
        model.addAttribute("limite", ServicoService.LIMITE_SAP);
        model.addAttribute("porcentagem", Math.min(100, n * 100 / ServicoService.LIMITE_SAP));
        model.addAttribute("estourou", n > ServicoService.LIMITE_SAP);
        model.addAttribute("similares", service.similares(nome));
        model.addAttribute("erro", erro);

        model.addAttribute("categorias", categoriaService.listarTodasCategorias());
        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("descricaoServico", descricao == null ? "" : descricao);
        model.addAttribute("unidadeMedidaId", unidadeMedidaId);

        model.addAttribute("unidadesCusto", adicionaisService.listarTodasUnidadesCustos());
        model.addAttribute("unidadeCustoId", unidadeCustoId);
        model.addAttribute("quantidade", quantidade == null ? "" : quantidade);
        model.addAttribute("observacao", observacao == null ? "" : observacao);
        model.addAttribute("maxObservacao", QuantidadeServico.MAX_OBSERVACAO);
        return "servicos/cadastro";
    }
}
