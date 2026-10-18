package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.repository.UnidadeCustoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UnidadeCustoService {

    private static final List<String> ORDEM_GRUPOS =
            List.of("Jornada", "Noturno", "Periculosidade", "Insalubridade", "Contratação");

    private final UnidadeCustoRepository repository;

    public UnidadeCustoService(UnidadeCustoRepository repository){
        this.repository = repository;
    }

    public List<UnidadeCusto> listarTodos(){
        return repository.findAll();
    }

    public Map<String, List<UnidadeCusto>> listarAdicionaisAgrupados() {
        Map<String, List<UnidadeCusto>> grupos = new LinkedHashMap<>();
        ORDEM_GRUPOS.forEach(g -> grupos.put(g, new ArrayList<>()));

        for (UnidadeCusto u : repository.findAll(Sort.by("id"))) {
            String grupo = grupoDe(u.getMaoDeObra());
            if (grupo != null) grupos.get(grupo).add(u);
        }
        grupos.values().removeIf(List::isEmpty);
        return grupos;
    }

    private String grupoDe(String maoDeObra) {
        String n = Normalizer.normalize(maoDeObra, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
        if (n.contains("nao sao mao de obra")) return null;
        if (n.contains("noturn"))    return "Noturno";
        if (n.contains("periculos")) return "Periculosidade";
        if (n.contains("insalubr"))  return "Insalubridade";
        if (n.startsWith("hora"))    return "Jornada";
        return "Contratação";
    }

}
