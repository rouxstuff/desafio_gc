package gerdauGrupo16.desafiogc.service.reguaAderencia;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tudo que a régua precisa de um nome, calculado UMA vez (no carregamento do índice) e não a cada comparação.
 *
 * @param normalizado texto canônico (ver NormalizadorTexto)
 * @param tokens      palavras do texto normalizado
 * @param numeros     palavras com algum dígito (25MM, 50, 1/2...): a "trava" de medidas
 * @param modificadores palavras como HE, HN, NOT: a "trava" de condição de trabalho
 * @param trigramas   pedaços de 3 letras, para tolerar erro de digitação e truncamento
 */
public record PerfilTexto(String normalizado, Set<String> tokens, Set<String> numeros,
                          Set<String> modificadores, Set<String> trigramas) {

    public static PerfilTexto de(String normalizado, Set<String> palavrasModificadoras) {
        Set<String> tokens = normalizado.isEmpty() ? Set.of()
                : new HashSet<>(Arrays.asList(normalizado.split(" ")));
        Set<String> numeros = new HashSet<>();
        Set<String> mods = new HashSet<>();
        for (String t : tokens) {
            if (t.chars().anyMatch(Character::isDigit)) numeros.add(t);
            if (palavrasModificadoras.contains(t)) mods.add(t);
        }
        Set<String> trigramas = new HashSet<>();
        String preenchido = " " + normalizado + " ";
        for (int i = 0; i + 3 <= preenchido.length(); i++) trigramas.add(preenchido.substring(i, i + 3));
        return new PerfilTexto(normalizado, tokens, numeros, mods, trigramas);
    }
}
