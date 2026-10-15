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
    public String consultar(@RequestParam(required = false) String descricao, Model model) {
        boolean buscou = descricao != null && !descricao.isBlank();

        model.addAttribute("Buscou", buscou);
        model.addAttribute("descricao", descricao);
        model.addAttribute("servicos", buscou ? service.buscarPorDescricao(descricao) : List.of());

        return "consulta";
    }
}
