package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: FornecedorController.java
 * TIPO: controller REST (@RestController), rota /fornecedor, restrito ao ADMIN.
 * FUNÇÃO: API de fornecedores e do vínculo fornecedor x serviço.
 * MÉTODOS:
 * - buscarFornecedores(): GET /fornecedor. Lista todos os fornecedores.
 * - buscarCodigoFornecedor(codigoFornecedor): GET /fornecedor/codigoFornecedor. Busca pelo código exato.
 * - vincularServico(fornecedorId, codigoServico): POST /fornecedor/{id}/servicos/{codigo}. Vincula o
 *   fornecedor a um serviço APROVADO. Respostas: 201 criado, 404 não existe, 409 já vinculado ou serviço
 *   não aprovado.
 * - listarServicosDoFornecedor(fornecedorId): GET /fornecedor/{id}/servicos. Serviços do fornecedor.
 */

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

    @PostMapping("/{fornecedorId}/servicos/{codigoServico}")
    public ResponseEntity<Void> vincularServico(@PathVariable("fornecedorId") Long fornecedorId,
                                                @PathVariable("codigoServico") String codigoServico) {
        fornecedorService.vincularServico(fornecedorId, codigoServico);
        return ResponseEntity.created(URI.create("/fornecedor/" + fornecedorId + "/servicos")).build();
    }

    @GetMapping("/{fornecedorId}/servicos")
    public List<Servico> listarServicosDoFornecedor(@PathVariable("fornecedorId") Long fornecedorId) {
        return fornecedorService.listarServicosDoFornecedor(fornecedorId);
    }
}
