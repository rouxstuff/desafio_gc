package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: FornecedorServicoService.java
 * TIPO: classe de serviço (@Service, construtor gerado pelo Lombok).
 * FUNÇÃO: leituras dos vínculos fornecedor x serviço.
 * MÉTODOS:
 * - buscarTodos(): todos os vínculos.
 * - buscarFornecedoresDoServico(codigoServico): fornecedores que atendem o serviço.
 * - buscarVinculosServico(codigoServico): vínculos de um serviço.
 */

import gerdauGrupo16.desafiogc.model.entity.FornecedorServico;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.repository.FornecedorRepository;
import gerdauGrupo16.desafiogc.repository.FornecedorServicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class FornecedorServicoService {

    private final FornecedorServicoRepository fornecedorServicoRepository;

    public List<FornecedorServico> buscarTodos(){
        return fornecedorServicoRepository.findAll();
    }

    public List<gerdauGrupo16.desafiogc.model.entity.Fornecedor> buscarFornecedoresDoServico(String codigoServico){
        return fornecedorServicoRepository.listarFornecedoresDoServico(codigoServico);
    }

    public List<FornecedorServico> buscarVinculosServico(String codigoServico){
        return fornecedorServicoRepository.findByServico_CodigoServico(codigoServico);
    }
}
