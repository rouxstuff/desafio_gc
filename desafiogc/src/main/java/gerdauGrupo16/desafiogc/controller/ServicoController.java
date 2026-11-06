package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: ServicoController.java
 * TIPO: controller REST (@RestController), rota /api/servicos.
 * FUNÇÃO: API de consulta e de criação de serviços (JSON), usada pelo Swagger e por integrações. Serviços
 *   Quem não é administrador recebe só os APROVADOS.
 * MÉTODOS:
 * - listar(pagina, tamanho, auth): GET /api/servicos. Lista paginada (padrão 100 por página, máximo 500).
 * - buscarCategoriaNome_ServicoNome(categoriaNome, nomeServico, auth): GET
 *   /api/servicos/categoriaNomeServico. Filtra por categoria e/ou nome.
 * - buscar(nome, pagina, tamanho, auth): GET /api/servicos/busca. Busca por nome ou código.
 * - sugestoes(categoriaId, nome): GET /api/servicos/sugestoes. Serviços parecidos com a nota da régua de
 *   aderência (sem categoriaId compara com todas).
 * - criarNovo(dto, principal): POST /api/servicos. Cria o serviço como PENDENTE. Respostas: 201, 400
 *   validação, 404 categoria, 409 duplicado.
 */

import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.controller.dto.SugestaoDTO;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Servico> listar(@RequestParam(defaultValue = "0") int pagina,
                                @RequestParam(defaultValue = "100") int tamanho, Authentication auth) {
        return service.listarPagina(null, pagina, tamanho, PerfilAcesso.ehAdmin(auth)).getContent();
    }

    @GetMapping("/categoriaNomeServico")
    public List<Servico> buscarCategoriaNome_ServicoNome(@RequestParam(required = false) String categoriaNome,
                                                         @RequestParam(required = false) String nomeServico,
                                                         Authentication auth) {
        return  service.buscarPorCategoriaNomeEServicoNome(categoriaNome, nomeServico, PerfilAcesso.ehAdmin(auth));
    }

    @GetMapping("/busca")
    public List<Servico> buscar(@RequestParam String nome,
                                @RequestParam(defaultValue = "0") int pagina,
                                @RequestParam(defaultValue = "100") int tamanho, Authentication auth) {
        return service.listarPagina(nome, pagina, tamanho, PerfilAcesso.ehAdmin(auth)).getContent();
    }

    @GetMapping("/sugestoes")
    public List<SugestaoDTO> sugestoes(@RequestParam(required = false) Long categoriaId,
                                       @RequestParam String nome) {
        return service.buscarSugestoes(categoriaId, nome);
    }

    @PostMapping
    public ResponseEntity<Servico> criarNovo(@Valid @RequestBody ServicoDTO dto, Principal principal) {
        Servico criado = service.criarNovo(dto, principal.getName());
        return ResponseEntity.created(URI.create("/api/servicos/" + criado.getCodigoServico())).body(criado);
    }
}
