package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: TermosBusca.java
 * TIPO: classe utilitária (final, só métodos estáticos).
 * FUNÇÃO: pesquisa múltipla da Consulta: vários termos separados por ponto e vírgula. Tira espaços das
 *   pontas, ignora termos vazios, conta uma vez só os repetidos (sem diferenciar maiúsculas nem acentos) e
 *   vale no máximo 10 termos.
 * MÉTODOS:
 * - separar(digitado): devolve a lista de termos, mantendo o texto original e a ordem digitada.
 * - chave(termo): forma comparável do termo (maiúsculas, sem acento, espaços simples).
 */

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TermosBusca {

    public static final char SEPARADOR = ';';
    public static final int MAX_TERMOS = 10;

    private TermosBusca() { }

    public static List<String> separar(String digitado) {
        if (digitado == null || digitado.isBlank()) return List.of();
        Map<String, String> unicos = new LinkedHashMap<>();
        for (String parte : digitado.split(String.valueOf(SEPARADOR))) {
            String termo = parte.strip();
            if (termo.isEmpty()) continue;
            unicos.putIfAbsent(chave(termo), termo);
            if (unicos.size() >= MAX_TERMOS) break;
        }
        return new ArrayList<>(unicos.values());
    }

    private static String chave(String termo) {
        String semAcento = Normalizer.normalize(termo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
