package gerdauGrupo16.desafiogc.service;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Regras do e-mail usado como login: sempre guardado em minúsculas e sem espaços nas pontas,
 * então "Maria@Empresa.com " e "maria@empresa.com" são o mesmo usuário.
 * A validação é propositalmente simples (algo@dominio.ext): confirmar que a caixa existe só é possível enviando e-mail.
 */
public final class EmailUsuario {

    public static final int MAX = 254;
    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private EmailUsuario() { }

    public static String normalizar(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    /** Recebe o e-mail já normalizado. */
    public static boolean valido(String emailNormalizado) {
        return emailNormalizado != null
                && !emailNormalizado.isEmpty()
                && emailNormalizado.length() <= MAX
                && FORMATO.matcher(emailNormalizado).matches();
    }
}
