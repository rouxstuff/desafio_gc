package gerdauGrupo16.desafiogc.service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Regra da quantidade informada ao contratar (ou solicitar) um serviço, de acordo com a unidade de medida.
 *
 *  - Unidades "contáveis" (ST = peça, ZCJ = conjunto, SV = serviço): só número inteiro.
 *  - As demais (KG, TO, M, M2, M3, L, H, MON, TAG...): decimal com vírgula, até 3 casas (ex.: 2,5).
 *  - Sempre maior que zero e no máximo 9.999.999,999. Ponto não é aceito (evita confundir "1.500" com 1,5).
 */
public final class QuantidadeServico {

    public static final Set<String> UNIDADES_INTEIRAS = Set.of("ST", "ZCJ", "SV");
    public static final BigDecimal MAXIMO = new BigDecimal("9999999.999");
    public static final int MAX_OBSERVACAO = 200;
    private static final Pattern FORMATO = Pattern.compile("\\d+(,\\d+)?");

    /** erro == null significa válido. valor é null quando a quantidade é opcional e não foi informada. */
    public record Resultado(BigDecimal valor, String erro) { }

    private QuantidadeServico() { }

    public static boolean inteira(String siglaUnidade) {
        return siglaUnidade != null && UNIDADES_INTEIRAS.contains(siglaUnidade.strip().toUpperCase());
    }

    public static String dica(String siglaUnidade) {
        return inteira(siglaUnidade)
                ? "Informe um número inteiro (ex.: 10)."
                : "Use vírgula para decimais (ex.: 2,5), até 3 casas.";
    }

    public static Resultado validar(String texto, String siglaUnidade, boolean obrigatoria) {
        String t = texto == null ? "" : texto.strip();
        if (t.isEmpty()) {
            return obrigatoria ? new Resultado(null, "Informe a quantidade.") : new Resultado(null, null);
        }
        if (siglaUnidade == null || siglaUnidade.isBlank()) {
            return new Resultado(null, "Escolha a unidade de medida para informar a quantidade.");
        }
        if (t.contains(".")) {
            return new Resultado(null, "Não use ponto. Use vírgula para decimais (ex.: 2,5) e não use separador de milhar.");
        }
        if (!FORMATO.matcher(t).matches()) {
            return new Resultado(null, "Quantidade inválida. Digite só números (ex.: 10 ou 2,5).");
        }
        if (inteira(siglaUnidade) && t.contains(",")) {
            return new Resultado(null, "Na unidade " + siglaUnidade.strip().toUpperCase() + " a quantidade deve ser um número inteiro.");
        }
        BigDecimal valor = new BigDecimal(t.replace(',', '.'));
        if (valor.scale() > 3) {
            return new Resultado(null, "Use no máximo 3 casas decimais.");
        }
        if (valor.signum() <= 0) {
            return new Resultado(null, "A quantidade deve ser maior que zero.");
        }
        if (valor.compareTo(MAXIMO) > 0) {
            return new Resultado(null, "Quantidade muito grande (máximo 9.999.999,999).");
        }
        return new Resultado(valor, null);
    }

    /** 10 -> "10"; 2.50 -> "2,5". */
    public static String formatar(BigDecimal valor) {
        if (valor == null) return "";
        return valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
