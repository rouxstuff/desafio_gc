package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.repository.UnidadeCustoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@AllArgsConstructor
public class UnidadeCustoService {

    private final UnidadeCustoRepository repository;

    public List<UnidadeCusto> listarTodasUnidadesCustos(){
        return repository.findAll(Sort.by(Sort.Direction.ASC, "maoDeObra"));
    }
}
