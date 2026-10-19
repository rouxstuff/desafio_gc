package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.repository.CategoriaRepository;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {
    public static final int LIMITE_SAP = 40;

    private final ServicoRepository repository;

    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }

    public List<Servico> listarTodos() {
        return repository.findAll();
    }

    public List<Servico> buscarPorNome(String nome) {
        return repository.findByNomeServicoContaining(padronizar(nome));
    }
    public List<Servico> buscarPorCategoriaNomeEServicoNome(String categoriaNome, String nomeServico) {
        return repository.findByCategoria_CategoriaNomeAndNomeServicoContaining(padronizar(categoriaNome), padronizar(nomeServico));
    }

    public Optional<Servico> buscarPorNomeExato(String nome) {
        return repository.findByNomeServico(padronizar(nome));
    }

    public List<Servico> buscarCandidatos(String digitado) {
        String d = padronizar(digitado);
        List<Servico> candidatos = new ArrayList<>();
        if (d.isEmpty()) return candidatos;

        // depois esse metodo sera substituido pela busca por similaridade
        for (Servico s : repository.findAll()) {
            String nome = padronizar(s.getNomeServico());
            if (nome.contains(d) || d.contains(nome)) {
                candidatos.add(s);
            }
        }
        return candidatos;
    }

    public List<Servico> similares(String nome) {
        List<Servico> todos = repository.findAll();
        List<Servico> parecidos = new ArrayList<>();

        for (String palavra : padronizar(nome).split(" ")) {
            if (palavra.length() <= 3) continue;
            String comeco = palavra.substring(0, 4);
            for (Servico s : todos) {
                if (padronizar(s.getNomeServico()).contains(comeco) && !parecidos.contains(s)) {
                    parecidos.add(s);
                }
            }
        }

        return parecidos.subList(0, Math.min(5, parecidos.size()));
    }

    public String tratarTextoExtra(String digitado, Servico servico) {
        String d = padronizar(digitado);
        String nome = padronizar(servico.getNomeServico());
        if (d.equals(nome) || !d.contains(nome)) return "";
        return d.replace(nome, "").trim();
    }

    public String padronizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
