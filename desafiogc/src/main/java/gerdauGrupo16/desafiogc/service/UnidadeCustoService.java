package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.repository.UnidadeCustoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UnidadeCustoService {

    private final UnidadeCustoRepository repository;

    public UnidadeCustoService(UnidadeCustoRepository repository){
        this.repository = repository;
    }

    public List<UnidadeCusto> listarTodos(){
        return repository.findAll();
    }
}
