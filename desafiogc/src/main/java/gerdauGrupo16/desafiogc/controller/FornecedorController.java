package gerdauGrupo16.desafiogc.controller;

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.service.FornecedorService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
