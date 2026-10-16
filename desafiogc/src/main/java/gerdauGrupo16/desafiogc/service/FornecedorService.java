package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.repository.FornecedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    public List<Fornecedor> buscarTodos() {
        return fornecedorRepository.findAll();
    }

    public List<Fornecedor> buscarCodigoFornecedor(String codigoFornecedor) {
        return fornecedorRepository.findByCodigoFornecedor(codigoFornecedor);
    }
}
