package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.repository.UnidadeMedidaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadeMedidaService {

    private final UnidadeMedidaRepository repository;

    public UnidadeMedidaService(UnidadeMedidaRepository repository) {
        this.repository = repository;
    }

    public List<UnidadeMedida> listarTodos(){
        return repository.findAll();
    }

    // [NOVO, do Kaio] ordenada por descrição, para o dropdown e o GET /unidades-medida
    public List<UnidadeMedida> listarTodasUnidadesMedidas(){
        return repository.findAll(Sort.by(Sort.Direction.ASC, "descricao"));
    }
}
