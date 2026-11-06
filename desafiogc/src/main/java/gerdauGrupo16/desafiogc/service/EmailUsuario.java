package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: EmailUsuario.java
 * TIPO: classe utilitária (final, só métodos estáticos).
 * FUNÇÃO: regras do e-mail usado como login: guardado em minúsculas e sem espaços nas pontas. A validação é
 *   simples (algo@dominio.ext, até 254 caracteres); confirmar que a caixa existe só é possível enviando
 *   e-mail.
 * MÉTODOS:
 * - normalizar(email): tira espaços e passa para minúsculas; nulo vira texto vazio.
 * - valido(emailNormalizado): confere o formato e o tamanho.
 */

import java.util.Locale;
import java.util.regex.Pattern;

public final class EmailUsuario {

    public static final int MAX = 254;
    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private EmailUsuario() { }

    public static String normalizar(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean valido(String emailNormalizado) {
        return emailNormalizado != null
                && !emailNormalizado.isEmpty()
                && emailNormalizado.length() <= MAX
                && FORMATO.matcher(emailNormalizado).matches();
    }
}
