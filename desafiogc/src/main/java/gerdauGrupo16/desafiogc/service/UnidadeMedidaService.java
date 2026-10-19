package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.repository.UnidadeMedidaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UnidadeMedidaService {

    private final UnidadeMedidaRepository repository;

    public List<UnidadeMedida> listarTodasUnidadesMedidas(){
        return repository.findAll(Sort.by(Sort.Direction.ASC, "descricao"));
    }
}
