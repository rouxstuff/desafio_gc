package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: CategoriaController.java
 * TIPO: controller REST (@RestController), rota /categoria.
 * FUNÇÃO: API de consulta de categorias (Civil, Eletromecânica, Facilities, Materiais, Insumos), devolvendo
 *   id e nome.
 * MÉTODOS:
 * - listarCategorias(): GET /categoria. Todas as categorias.
 * - buscarPorCategoria(categoriaNome): GET /categoria/busca. Categorias cujo nome contém o texto informado.
 */

import gerdauGrupo16.desafiogc.controller.dto.CategoriaDTO;
import gerdauGrupo16.desafiogc.service.CategoriaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categoria")
@AllArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        List<CategoriaDTO> listaCategorias = categoriaService.listarTodasCategorias().stream().
                map(categoria -> new CategoriaDTO(categoria.getId(), categoria.getCategoriaNome())).
                toList();
        return ResponseEntity.ok(listaCategorias);
    }

    @GetMapping("/busca")
    public ResponseEntity<List<CategoriaDTO>> buscarPorCategoria(@RequestParam String categoriaNome) {
        List<CategoriaDTO> lista = categoriaService.buscarCategoriaNome(categoriaNome)
                .stream()
                .map(categoria -> new CategoriaDTO(categoria.getId(), categoria.getCategoriaNome()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
}
