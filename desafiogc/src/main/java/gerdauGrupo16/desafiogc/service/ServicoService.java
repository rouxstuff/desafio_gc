package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.Servico;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class ServicoService {

    private ServicoRepository repository;

    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }

    public List<Servico> listarTodos() {
        return repository.findAll();
    }

    public List<Servico> buscarPorDescricao(String descricao) {
        return repository.findByDescricaoContaining(padronizar(descricao));
    }

    private String padronizar(String texto) {
        String semAcento = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toUpperCase();
    }


}
