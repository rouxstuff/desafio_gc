<<<<<<< HEAD
package gerdauGrupo16.desafiogc.controller;

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
    public String consultar(@RequestParam(required = false) String nomeServico, Authentication auth, Model model,
                            RedirectAttributes redirect) {
        boolean admin = ehAdmin(auth);   // serviço rejeitado só aparece para o administrador
        boolean buscou = nomeServico != null && !nomeServico.isBlank();
        model.addAttribute("nomeServico", nomeServico);
        model.addAttribute("multiplo", false);

        // [PESQUISA MÚLTIPLA] "Ajudante;Mecanico;Almoço" pesquisa os três de uma vez (separador: ponto e vírgula)
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
            return "servicos/busca";   // com vários termos sempre mostra a lista (sem pular direto para os detalhes)
        }

        // um termo só (inclui "ajudante;" com ponto e vírgula sobrando): comportamento de sempre
        String termo = termos.get(0);
        model.addAttribute("nomeServico", termo);   // sem o ";" sobrando
        List<Servico> candidatos = service.buscarCandidatos(termo, admin);

        if (candidatos.isEmpty()) {
            return paginaNaoEncontrado(service.padronizar(termo), model);
        }
        if (candidatos.size() == 1) {
            // [ALTERADO] a chave agora é o CÓDIGO (o nome pode repetir em categorias diferentes)
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
        // o administrador não contrata: ele altera as características do serviço
        if (ehAdmin(auth)) return "redirect:/admin/servicos/" + codigo;
        Optional<Servico> achado = service.buscarPorCodigo(codigo)
                .filter(s -> ServicoService.visivelPara(s, false));   // rejeitado não existe para o usuário
        if (achado.isEmpty()) { // Se alguem mexer na URL com um código que nao existe, mandamos de volta para a busca
            return "redirect:/servicos";
        }
        return paginaDetalhes(achado.get(), digitado, null, null, null, null, ehAdmin(auth), model);
    }

    private static boolean ehAdmin(Authentication auth) {
        return PerfilAcesso.ehAdmin(auth);
    }

    /**
     * Serviço APROVADO visto por quem não é administrador: a unidade de medida vem do catálogo e não pode ser
     * escolhida (só o administrador atribui; se ainda não tiver unidade, fica sem). Os adicionais do contrato
     * qualquer usuário pode marcar.
     */
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
        // [NOVO] o contrato também escolhe a unidade de medida (vem preenchida se o serviço já tem uma)
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
        if (ehAdmin(auth)) return "redirect:/admin/servicos/" + codigo;   // admin não contrata
        Optional<Servico> achado = service.buscarPorCodigo(codigo)
                .filter(s -> ServicoService.visivelPara(s, false));   // rejeitado não existe para o usuário
        if (achado.isEmpty()) {
            return "redirect:/servicos";
        }
        Servico servico = achado.get();
        boolean admin = false;

        // Serviço aprovado e usuário que não é admin: a unidade vem do catálogo (ignora a unidade que vier no POST,
        // mesmo que alguém monte a requisição à mão). Os adicionais continuam valendo.
        boolean contrato = modoContrato(servico, admin);
        UnidadeMedida unidade = contrato ? servico.getUnidadeMedida() : buscarUnidade(unidadeMedidaId);

        // sem unidade definida pelo admin não há como informar quantidade
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
        // [NOVO] unidade de medida escolhida (opcional)
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

    /** Cadastra de verdade (status PENDENTE). O código é gerado pelo sistema (ex.: FAC-000001). */
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
        // quantidade (opcional) conforme a unidade de medida escolhida; observação opcional
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
            // 409 duplicado / 404 não encontrado: mostra a mensagem no próprio formulário
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
        // [NOVO] dropdowns e valores já digitados (para não perder o que o usuário preencheu ao dar erro)
        model.addAttribute("categorias", categoriaService.listarTodasCategorias());
        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("descricaoServico", descricao == null ? "" : descricao);
        model.addAttribute("unidadeMedidaId", unidadeMedidaId);
        // [NOVO] unidade de custo (opcional), igual à de medida
        model.addAttribute("unidadesCusto", adicionaisService.listarTodasUnidadesCustos());
        model.addAttribute("unidadeCustoId", unidadeCustoId);
        model.addAttribute("quantidade", quantidade == null ? "" : quantidade);
        model.addAttribute("observacao", observacao == null ? "" : observacao);
        model.addAttribute("maxObservacao", QuantidadeServico.MAX_OBSERVACAO);
        return "servicos/cadastro";
    }
}
=======
package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.service.QuantidadeServico;
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

import java.security.Principal;
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
        return paginaDetalhes(achado.get(), digitado, null, null, null, null, model);
    }

    /** Serviço APROVADO que já tem unidade de medida: só pede quantidade e observação (unidade e adicionais já estão definidos). */
    private boolean modoContrato(Servico servico) {
        return servico.getStatus() == StatusServico.APROVADO && servico.getUnidadeMedida() != null;
    }

    private String paginaDetalhes(Servico servico, String digitado, String erro, String quantidade,
                                  String observacao, Long unidadeMedidaId, Model model) {
        boolean contrato = modoContrato(servico);
        model.addAttribute("nomeServico", servico.getNomeServico());
        model.addAttribute("servico", servico);
        model.addAttribute("digitado", digitado == null ? "" : digitado);
        model.addAttribute("textoExtra", service.tratarTextoExtra(digitado, servico));
        model.addAttribute("grupos", adicionaisService.listarAdicionaisAgrupados());
        // [NOVO] o contrato também escolhe a unidade de medida (vem preenchida se o serviço já tem uma)
        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        model.addAttribute("modoContrato", contrato);
        model.addAttribute("dicaQuantidade", contrato ? QuantidadeServico.dica(servico.getUnidadeMedida().getCodigo()) : "");
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
                            Model model) {
        Optional<Servico> achado = service.buscarPorCodigo(codigo);
        if (achado.isEmpty()) {
            return "redirect:/servicos";
        }
        Servico servico = achado.get();

        // Serviço aprovado com unidade de medida: a unidade vem do catálogo e não há adicionais (ignora o que vier no POST).
        boolean contrato = modoContrato(servico);
        UnidadeMedida unidade = contrato ? servico.getUnidadeMedida() : buscarUnidade(unidadeMedidaId);
        if (contrato) ids = null;

        QuantidadeServico.Resultado qtd = QuantidadeServico.validar(quantidade, unidade == null ? null : unidade.getCodigo(), contrato);
        String obs = observacao == null ? "" : observacao.strip();
        String erro = qtd.erro();
        if (erro == null && obs.length() > QuantidadeServico.MAX_OBSERVACAO) {
            erro = "A observação deve ter no máximo " + QuantidadeServico.MAX_OBSERVACAO + " caracteres.";
        }
        if (erro != null) {
            return paginaDetalhes(servico, digitado, erro, quantidade, observacao, unidadeMedidaId, model);
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

    /** Cadastra de verdade (status PENDENTE). O código é gerado pelo sistema (ex.: FAC-000001). */
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
        // quantidade (opcional) conforme a unidade de medida escolhida; observação opcional
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
            // 409 duplicado / 404 não encontrado: mostra a mensagem no próprio formulário
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
        // [NOVO] dropdowns e valores já digitados (para não perder o que o usuário preencheu ao dar erro)
        model.addAttribute("categorias", categoriaService.listarTodasCategorias());
        model.addAttribute("unidadesMedida", unidadeMedidaService.listarTodasUnidadesMedidas());
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("descricaoServico", descricao == null ? "" : descricao);
        model.addAttribute("unidadeMedidaId", unidadeMedidaId);
        // [NOVO] unidade de custo (opcional), igual à de medida
        model.addAttribute("unidadesCusto", adicionaisService.listarTodasUnidadesCustos());
        model.addAttribute("unidadeCustoId", unidadeCustoId);
        model.addAttribute("quantidade", quantidade == null ? "" : quantidade);
        model.addAttribute("observacao", observacao == null ? "" : observacao);
        model.addAttribute("maxObservacao", QuantidadeServico.MAX_OBSERVACAO);
        return "servicos/cadastro";
    }
}
>>>>>>> 98d0b438cd52f872c6c87d7e52ad78bc0effa892
