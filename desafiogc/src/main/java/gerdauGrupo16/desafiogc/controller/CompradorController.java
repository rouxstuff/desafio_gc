package gerdauGrupo16.desafiogc.controller;

/*
 * ARQUIVO: CompradorController.java
 * TIPO: controller REST (@RestController), rota /comprador, restrito ao ADMIN.
 * FUNÇÃO: API de compradores e do vínculo comprador x serviço.
 * MÉTODOS:
 * - buscarCompradores(): GET /comprador. Lista todos os compradores.
 * - buscarCodigoComprador(codigoComprador): GET /comprador/codigoComprador. Busca pelo código exato.
 * - vincularServico(compradorId, codigoServico): POST /comprador/{id}/servicos/{codigo}. Vincula o
 *   comprador a um serviço APROVADO. Respostas: 201 criado, 404 não existe, 409 já vinculado ou serviço
 *   não aprovado.
 * - listarServicosDoComprador(compradorId): GET /comprador/{id}/servicos. Serviços do comprador.
 */

import gerdauGrupo16.desafiogc.model.entity.Comprador;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.service.CompradorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/comprador")
@AllArgsConstructor
public class CompradorController {

    private final CompradorService compradorService;

    @GetMapping
    public List<Comprador> buscarCompradores() {
        return compradorService.buscarTodos();
    }

    @GetMapping("/codigoComprador")
    public List<Comprador> buscarCodigoComprador(@RequestParam String codigoComprador) {
        return compradorService.buscarCodigoComprador(codigoComprador);
    }

    @PostMapping("/{compradorId}/servicos/{codigoServico}")
    public ResponseEntity<Void> vincularServico(@PathVariable("compradorId") Long compradorId,
                                                @PathVariable("codigoServico") String codigoServico) {
        compradorService.vincularServico(compradorId, codigoServico);
        return ResponseEntity.created(URI.create("/comprador/" + compradorId + "/servicos")).build();
    }

    @GetMapping("/{compradorId}/servicos")
    public List<Servico> listarServicosDoComprador(@PathVariable("compradorId") Long compradorId) {
        return compradorService.listarServicosDoComprador(compradorId);
    }
}
