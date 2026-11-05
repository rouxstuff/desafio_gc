package gerdauGrupo16.desafiogc.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pesquisa múltipla na tela de consulta: vários termos separados por ponto e vírgula.
 * Ex.: "Ajudante;Mecanico;Almoço" vira ["Ajudante", "Mecanico", "Almoço"].
 *
 * Regras: espaços nas pontas são tirados; termos vazios ("a;;b" ou "a;") são ignorados; termos repetidos
 * (sem diferenciar maiúsculas nem acentos) contam uma vez só; vale no máximo {@link #MAX_TERMOS} termos
 * (os primeiros), para uma digitação enorme não virar dezenas de buscas.
 */
public final class TermosBusca {

    public static final char SEPARADOR = ';';
    public static final int MAX_TERMOS = 10;

    private TermosBusca() { }

    /** Mantém o texto original de cada termo (para mostrar ao usuário) e a ordem em que foram digitados. */
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

    /** Mesma ideia do padronizar() do ServicoService: maiúsculas, sem acento, espaços simples. */
    private static String chave(String termo) {
        String semAcento = Normalizer.normalize(termo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
