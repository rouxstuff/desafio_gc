package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: UnidadeMedidaController.java
 * TIPO: controller REST (@RestController), rota /unidades-medida.
 * FUNÇÃO: API de consulta das unidades de medida oficiais (H, M, M2, KG, SV, MON, ST...).
 * MÉTODOS:
 * - listar(): GET /unidades-medida. Devolve id e descrição de todas, em ordem alfabética.
 */

import gerdauGrupo16.desafiogc.controller.dto.UnidadeCustoDTO;
import gerdauGrupo16.desafiogc.controller.dto.UnidadeMedidaDTO;
import gerdauGrupo16.desafiogc.service.UnidadeCustoService;
import gerdauGrupo16.desafiogc.service.UnidadeMedidaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("unidades-medida")
@AllArgsConstructor
public class UnidadeMedidaController {

    private UnidadeMedidaService unidadeMedidaService;

    @GetMapping
    public ResponseEntity<List<UnidadeMedidaDTO>> listar() {
        List<UnidadeMedidaDTO> listaUnidades = unidadeMedidaService.listarTodasUnidadesMedidas().stream().
                map(unidadeMedida -> new UnidadeMedidaDTO(unidadeMedida.getId(),unidadeMedida.getDescricao())).toList();
        return ResponseEntity.ok(listaUnidades);
    }
}
