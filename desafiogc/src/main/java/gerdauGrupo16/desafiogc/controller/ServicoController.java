package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.model.Servico;
import gerdauGrupo16.desafiogc.service.ServicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Servico> listar() {
        return service.listarTodos();
    }

    @GetMapping("/busca")
    public List<Servico> buscar(@RequestParam String descricao) {
        return service.buscarPorDescricao(descricao);
    }
}
