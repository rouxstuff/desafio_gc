package gerdauGrupo16.desafiogc.controller;
import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.controller.dto.SugestaoDTO;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.ServicoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/servicos")
@AllArgsConstructor
public class ServicoController {

    private final ServicoService service;

    @GetMapping
    public List<Servico> listar() {
        return service.listarTodosServicos();
    }

    @GetMapping("/categoriaNome-ServicoNome")
    public List<Servico> buscarCategoriaNome_ServicoNome(@RequestParam(required = false) String categoriaNome, @RequestParam(required = false) String nomeServico) {
        return  service.buscarPorCategoriaNomeEServicoNome(categoriaNome, nomeServico);
    }

    @GetMapping("/busca")
    public List<Servico> buscar(@RequestParam String nome) {
        return service.buscarPorNome(nome);
    }

    @GetMapping("/sugestoes")
    public List<SugestaoDTO> sugestoes(@RequestParam("categoriaId") Long categoriaId,
                                       @RequestParam("nome") String nome) {
        return service.buscarSugestoes(categoriaId, nome);
    }

    @PostMapping
    public ResponseEntity<Servico> criarNovo(@Valid @RequestBody ServicoDTO dto) {
        Servico criado = service.criarNovo(dto);
        return ResponseEntity.created(URI.create("/servicos/" + criado.getCodigoServico())).body(criado);
    }
}
