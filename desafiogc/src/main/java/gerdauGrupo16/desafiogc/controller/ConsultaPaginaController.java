package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.ServicoService;
import gerdauGrupo16.desafiogc.service.UnidadeCustoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ConsultaPaginaController {

    private final ServicoService service;
    private final UnidadeCustoService adicionaisService;

    public ConsultaPaginaController(ServicoService service, UnidadeCustoService adicionaisService) {
        this.service = service;
        this.adicionaisService = adicionaisService;
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
            redirect.addAttribute("nome", candidatos.get(0).getNomeServico());
            redirect.addAttribute("digitado", nomeServico);
            return "redirect:/servicos/detalhes";
        }
        model.addAttribute("candidatos", candidatos);

        return "servicos/busca";
    }

    @GetMapping("/servicos/detalhes")
    public String detalhes(@RequestParam String nome,
                           @RequestParam(required = false) String digitado,
                           Model model, RedirectAttributes redirect) {
        Optional<Servico> achado = service.buscarPorNomeExato(nome);
        if (achado.isEmpty()) { // Se alguem mexer na URL com um nome que nao existe, mandamos de volta para a busca
            redirect.addAttribute("nomeServico", nome);
            return "redirect:/servicos";
        }
        Servico servico = achado.get();

        model.addAttribute("nomeServico", servico.getNomeServico());
        model.addAttribute("servico", servico);
        model.addAttribute("textoExtra", service.tratarTextoExtra(digitado, servico));
        model.addAttribute("grupos", adicionaisService.listarAdicionaisAgrupados());
        return "servicos/detalhes";
    }

    private String paginaNaoEncontrado(String nome, Model model) {
        model.addAttribute("nomeServico", nome);
        model.addAttribute("nomeNovo", nome);
        model.addAttribute("similares", service.similares(nome));
        return "servicos/nao-encontrado";
    }
}
