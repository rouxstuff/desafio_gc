package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: UnidadeMedidaService.java
 * TIPO: classe de serviço (@Service).
 * FUNÇÃO: leitura das unidades de medida.
 * MÉTODOS:
 * - listarTodos(): todas, sem ordem definida.
 * - listarTodasUnidadesMedidas(): todas, em ordem alfabética de descrição (listas das telas).
 */

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

    public List<UnidadeMedida> listarTodasUnidadesMedidas(){
        return repository.findAll(Sort.by(Sort.Direction.ASC, "descricao"));
    }
}
