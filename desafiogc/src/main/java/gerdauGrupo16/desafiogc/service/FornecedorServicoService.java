package gerdauGrupo16.desafiogc.service;

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

    /** [PERFORMANCE] Fornecedores de um serviço, em uma query. */
    public List<gerdauGrupo16.desafiogc.model.entity.Fornecedor> buscarFornecedoresDoServico(String codigoServico){
        return fornecedorServicoRepository.listarFornecedoresDoServico(codigoServico);
    }

    public List<FornecedorServico> buscarVinculosServico(String codigoServico){
        return fornecedorServicoRepository.findByServico_CodigoServico(codigoServico);
    }
}
