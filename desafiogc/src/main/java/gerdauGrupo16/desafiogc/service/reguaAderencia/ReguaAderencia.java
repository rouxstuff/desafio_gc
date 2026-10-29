package gerdauGrupo16.desafiogc.service.reguaAderencia;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReguaAderencia {
    public double calcular(String a, String b) {
        Set<String> pa = palavras(a);
        Set<String> pb = palavras(b);
        if (pa.isEmpty() || pb.isEmpty()) return 0.0;

        if (pa.equals(pb)) return 1.0;                               // mesmas palavras, em qualquer ordem
        if (pa.containsAll(pb) || pb.containsAll(pa)) return 0.8;    // um nome está dentro do outro

        Set<String> comuns = new HashSet<>(pa); comuns.retainAll(pb);
        Set<String> todas  = new HashSet<>(pa); todas.addAll(pb);
        return (double) comuns.size() / todas.size();                // palavras em comum / total
    }

    private Set<String> palavras(String texto) {
        if (texto == null) return Set.of();
        String limpo = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")            // tira acentos
                .toUpperCase()
                .replaceAll("[^A-Z0-9 ]", " ");      // tira pontuação
        return Arrays.stream(limpo.trim().split("\\s+"))
                .filter(p -> !p.isBlank())
                .collect(Collectors.toSet());
    }
}
