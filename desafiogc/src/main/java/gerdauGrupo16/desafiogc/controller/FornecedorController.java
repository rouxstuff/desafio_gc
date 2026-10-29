package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.FornecedorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/fornecedor")
@AllArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public List<Fornecedor> buscarFornecedores() {
        return fornecedorService.buscarTodos();
    }

    @GetMapping("/codigoFornecedor")
    public List<Fornecedor> buscarCodigoFornecedor(@RequestParam String codigoFornecedor) {
        return fornecedorService.buscarCodigoFornecedor(codigoFornecedor);
    }

    // [NOVO, do Kaio] Vincula o fornecedor a um serviço APROVADO do catálogo.
    // 201 criado | 404 não existe | 409 já vinculado ou serviço não aprovado
    @PostMapping("/{fornecedorId}/servicos/{codigoServico}")
    public ResponseEntity<Void> vincularServico(@PathVariable("fornecedorId") Long fornecedorId,
                                                @PathVariable("codigoServico") String codigoServico) {
        fornecedorService.vincularServico(fornecedorId, codigoServico);
        return ResponseEntity.created(URI.create("/fornecedor/" + fornecedorId + "/servicos")).build();
    }

    // [NOVO, do Kaio] Lista os serviços do fornecedor.
    @GetMapping("/{fornecedorId}/servicos")
    public List<Servico> listarServicosDoFornecedor(@PathVariable("fornecedorId") Long fornecedorId) {
        return fornecedorService.listarServicosDoFornecedor(fornecedorId);
    }
}
