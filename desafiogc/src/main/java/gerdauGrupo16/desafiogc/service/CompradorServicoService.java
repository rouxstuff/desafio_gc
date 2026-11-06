package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: CompradorServicoService.java
 * TIPO: classe de serviço (@Service, construtor gerado pelo Lombok).
 * FUNÇÃO: leituras dos vínculos comprador x serviço.
 * MÉTODOS:
 * - buscarTodos(): todos os vínculos.
 * - buscarCompradoresDoServico(codigoServico): compradores que atendem o serviço.
 * - listarVinculosDoServico(codigoServico): vínculos de um serviço já com o comprador e a data do vínculo.
 * - buscarVinculosServico(codigoServico): vínculos de um serviço.
 */

import gerdauGrupo16.desafiogc.model.entity.CompradorServico;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.repository.CompradorRepository;
import gerdauGrupo16.desafiogc.repository.CompradorServicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class CompradorServicoService {

    private final CompradorServicoRepository compradorServicoRepository;

    public List<CompradorServico> buscarTodos(){
        return compradorServicoRepository.findAll();
    }

    public List<gerdauGrupo16.desafiogc.model.entity.Comprador> buscarCompradoresDoServico(String codigoServico){
        return compradorServicoRepository.listarCompradoresDoServico(codigoServico);
    }

    public List<CompradorServico> listarVinculosDoServico(String codigoServico){
        return compradorServicoRepository.listarVinculosDoServico(codigoServico);
    }

    public List<CompradorServico> buscarVinculosServico(String codigoServico){
        return compradorServicoRepository.findByServico_CodigoServico(codigoServico);
    }
}
