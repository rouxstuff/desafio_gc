package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.service.ServicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ConsultaPaginaController {

    private final ServicoService service;

    public ConsultaPaginaController(ServicoService service) {
        this.service = service;
    }

    @GetMapping("/servicos")
    public String consultar(@RequestParam(required = false) String nomeServico, Model model) {
        boolean buscou = nomeServico != null && !nomeServico.isBlank();

        model.addAttribute("nomeServico", nomeServico);
        model.addAttribute("candidatos", buscou ? service.buscarPorNome(nomeServico) : List.of());

        return "servicos/busca";
    }
}
