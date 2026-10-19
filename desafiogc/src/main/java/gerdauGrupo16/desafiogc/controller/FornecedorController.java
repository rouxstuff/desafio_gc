package gerdauGrupo16.desafiogc.controller;
import gerdauGrupo16.desafiogc.controller.dto.FornecedorDTO;
import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.FornecedorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/fornecedores")
@AllArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public ResponseEntity<List<FornecedorDTO>> listarFornecedores() {
        List<FornecedorDTO> lista = fornecedorService.listarTodosFornecedores().stream()
                .map(fornecedor -> new FornecedorDTO(fornecedor.getCodigoFornecedor(),fornecedor.getNomeFornecedor()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/busca")
    public List<Fornecedor> buscarCodigoFornecedor(@RequestParam String codigoFornecedor) {
        return fornecedorService.buscarCodigoFornecedor(codigoFornecedor);
    }

    // [NOVO] Vincula o fornecedor a um serviço existente do catálogo.
    // 201 criado | 404 fornecedor ou serviço inexistente | 409 já vinculado
    @PostMapping("/{fornecedorId}/servicos/{codigoServico}")
    public ResponseEntity<Void> vincularServico(@PathVariable("fornecedorId") Long fornecedorId,
                                                @PathVariable("codigoServico") String codigoServico) {
        fornecedorService.vincularServico(fornecedorId, codigoServico);
        return ResponseEntity.created(URI.create("/fornecedores/" + fornecedorId + "/servicos")).build();
    }

    // [NOVO] Lista os serviços do fornecedor.
    @GetMapping("/{fornecedorId}/servicos")
    public List<Servico> listarServicosDoFornecedor(@PathVariable("fornecedorId") Long fornecedorId) {
        return fornecedorService.listarServicosDoFornecedor(fornecedorId);
    }
}
