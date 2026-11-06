package gerdauGrupo16.desafiogc.service.reguaAderencia;

/*
 * ARQUIVO: PerfilTexto.java
 * TIPO: record.
 * FUNÇÃO: tudo que a régua precisa de um nome, calculado uma vez (no índice) e não a cada comparação.
 * CAMPOS: normalizado (texto padrão); tokens (palavras); numeros (palavras com dígito, como 25MM: trava de
 *   medidas); modificadores (HE, HN, NOT...: trava de condição de trabalho); trigramas (pedaços de 3
 *   letras, para tolerar erro de digitação e truncamento).
 * MÉTODOS:
 * - de(normalizado, palavrasModificadoras): monta o perfil a partir do texto já normalizado.
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
