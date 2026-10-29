package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.controller.dto.SugestaoDTO;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @GetMapping("/categoriaNomeServico")
    public List<Servico> buscarCategoriaNome_ServicoNome(@RequestParam(required = false) String categoriaNome,
                                                         @RequestParam(required = false) String nomeServico) {
        return  service.buscarPorCategoriaNomeEServicoNome(categoriaNome, nomeServico);
    }

    @GetMapping("/busca")
    public List<Servico> buscar(@RequestParam String nome) {
        return service.buscarPorNome(nome);
    }

    // [NOVO, do Kaio] Régua de aderência. Ex.: GET /api/servicos/sugestoes?categoriaId=1&nome=Ajudante Geral
    // (sem categoriaId compara com todas as categorias)
    @GetMapping("/sugestoes")
    public List<SugestaoDTO> sugestoes(@RequestParam(required = false) Long categoriaId,
                                       @RequestParam String nome) {
        return service.buscarSugestoes(categoriaId, nome);
    }

    // [NOVO, do Kaio] Cria o serviço como PENDENTE. 201 | 400 validação | 404 categoria | 409 duplicado
    @PostMapping
    public ResponseEntity<Servico> criarNovo(@Valid @RequestBody ServicoDTO dto) {
        Servico criado = service.criarNovo(dto);
        return ResponseEntity.created(URI.create("/api/servicos/" + criado.getCodigoServico())).body(criado);
    }
}
