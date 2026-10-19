package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.repository.CategoriaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> buscarCategoriaNome(String categoriaNome) {
        return categoriaRepository.findByCategoriaNomeContaining(padronizar(categoriaNome));
    }

    public List<Categoria> listarTodasCategorias() {
        return categoriaRepository.findAll(Sort.by(Sort.Direction.DESC, "categoriaNome"));
    }

    private String padronizar(String texto) {
        String semAcento = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
