package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: ContratacaoController.java
 * TIPO: controller de telas (@Controller), rota /contratacoes, para qualquer usuário logado.
 * FUNÇÃO: tela de contratações, com duas visões conforme o perfil. O comprador vê só os serviços que ele
 *   contratou (menu Meus serviços). O administrador vê a tela completa (menu Contratações): liga comprador a
 *   serviço APROVADO, pesquisa um serviço e vê os compradores vinculados com a data do vínculo, além das
 *   confirmações feitas pelos usuários. As regras ficam nos services; este controller só monta a tela.
 * CLASSES INTERNAS (records): LinhaComprador (id e rótulo, item da lista de compradores) e LinhaVinculo
 *   (rótulo do comprador e data/hora do vínculo).
 * MÉTODOS:
 * - listar(servicoFiltro, busca, auth, model): comprador: lista as próprias contratações (servicos/contratacoes).
 *   Administrador: abre admin/contratacoes. A pesquisa é por nome ou código (até 50 resultados, só serviços
 *   aprovados); se a busca achar um serviço só, ele já abre com seus compradores.
 * - vincular(compradorId, codigoServico, redirect): só administrador. Cria o vínculo
 *   (CompradorService.vincularServico) e volta para a tela com sucesso ou erro.
 * - montarCompradores(): monta a lista de compradores (id e rótulo) ordenada por id.
 * - rotulo(c): texto do comprador na tela: Comprador ID - nome, ou Comprador ID (8 primeiros caracteres do
 *   código) quando não há nome.
 */

import gerdauGrupo16.desafiogc.model.entity.Comprador;
import gerdauGrupo16.desafiogc.model.entity.CompradorServico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.repository.ServicoOpcaoView;
import gerdauGrupo16.desafiogc.service.CompradorService;
import gerdauGrupo16.desafiogc.service.CompradorServicoService;
import gerdauGrupo16.desafiogc.service.ContratacaoService;
import gerdauGrupo16.desafiogc.service.ServicoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Controller
public class ContratacaoController {

    private static final int MAX_RESULTADOS_BUSCA = 50;

    private final ContratacaoService contratacaoService;
    private final ServicoService servicoService;
    private final CompradorService compradorService;
    private final CompradorServicoService compradorServicoService;

    public ContratacaoController(ContratacaoService contratacaoService,
                                 ServicoService servicoService,
                                 CompradorService compradorService,
                                 CompradorServicoService compradorServicoService) {
        this.contratacaoService = contratacaoService;
        this.servicoService = servicoService;
        this.compradorService = compradorService;
        this.compradorServicoService = compradorServicoService;
    }

    public record LinhaComprador(Long id, String rotulo) { }

    public record LinhaVinculo(String rotulo, LocalDateTime vinculadoEm) { }

    @GetMapping("/contratacoes")
    public String listar(@RequestParam(value = "servicoFiltro", required = false) String servicoFiltro,
                         @RequestParam(value = "busca", required = false) String busca,
                         Authentication auth, Model model) {
        if (!PerfilAcesso.ehAdmin(auth)) {
            model.addAttribute("contratacoes", contratacaoService.listar(auth.getName()));
            return "servicos/contratacoes";
        }

        model.addAttribute("compradores", montarCompradores());
        model.addAttribute("servicosVinculaveis", servicoService.listarOpcoesPorStatus(StatusServico.APROVADO));
        model.addAttribute("contratacoes", contratacaoService.listarTodas());

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
            List<LinhaVinculo> vinculados = compradorServicoService.listarVinculosDoServico(servicoFiltro).stream()
                    .map((CompradorServico v) -> new LinhaVinculo(rotulo(v.getComprador()), v.getVinculadoEm()))
                    .toList();
            model.addAttribute("compradoresVinculados", vinculados);
        }
        model.addAttribute("servicoFiltro", servicoFiltro);
        return "admin/contratacoes";
    }

    @PostMapping("/contratacoes")
    public String vincular(@RequestParam(value = "compradorId", required = false) Long compradorId,
                           @RequestParam(value = "codigoServico", required = false) String codigoServico,
                           RedirectAttributes redirect) {
        if (compradorId == null || codigoServico == null || codigoServico.isBlank()) {
            redirect.addFlashAttribute("erro", "Escolha o comprador e o serviço para vincular.");
            return "redirect:/contratacoes";
        }
        try {
            compradorService.vincularServico(compradorId, codigoServico);
            redirect.addFlashAttribute("sucesso", "Comprador vinculado ao serviço " + codigoServico + ".");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/contratacoes";
    }

    private List<LinhaComprador> montarCompradores() {
        return compradorService.buscarTodos().stream()
                .sorted(Comparator.comparing(Comprador::getId))
                .map(c -> new LinhaComprador(c.getId(), rotulo(c)))
                .toList();
    }

    private String rotulo(Comprador c) {
        if (c.getNomeComprador() != null && !c.getNomeComprador().isBlank()) {
            return "Comprador " + c.getId() + " - " + c.getNomeComprador();
        }
        String codigo = c.getCodigoComprador() == null ? "" : c.getCodigoComprador();
        String curto = codigo.length() > 8 ? codigo.substring(0, 8) : codigo;
        return "Comprador " + c.getId() + (curto.isEmpty() ? "" : " (" + curto + ")");
    }
}
