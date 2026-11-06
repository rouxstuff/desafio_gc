package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: UnidadeCustoController.java
 * TIPO: controller REST (@RestController), rota /unidades-custo.
 * FUNÇÃO: API de consulta das unidades de custo (adicionais de mão de obra, como hora extra e adicional
 *   noturno).
 * MÉTODOS:
 * - listar(): GET /unidades-custo. Devolve id e nome de todas, em ordem alfabética.
 */

import gerdauGrupo16.desafiogc.controller.dto.CategoriaDTO;
import gerdauGrupo16.desafiogc.controller.dto.UnidadeCustoDTO;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.service.UnidadeCustoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("unidades-custo")
@AllArgsConstructor
public class UnidadeCustoController {

    private UnidadeCustoService unidadeCustoService;

    @GetMapping
    public ResponseEntity<List<UnidadeCustoDTO>> listar() {
        List<UnidadeCustoDTO> listaUnidades = unidadeCustoService.listarTodasUnidadesCustos().stream().
                map(unidadeCusto -> new UnidadeCustoDTO(unidadeCusto.getId(), unidadeCusto.getMaoDeObra())).toList();
        return ResponseEntity.ok(listaUnidades);
    }
}
