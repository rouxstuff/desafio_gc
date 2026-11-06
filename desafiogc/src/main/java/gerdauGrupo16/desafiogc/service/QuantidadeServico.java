package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: QuantidadeServico.java
 * TIPO: classe utilitária (final, só métodos estáticos).
 * FUNÇÃO: regra da quantidade informada ao contratar ou solicitar um serviço, conforme a unidade de medida.
 *   ST, ZCJ e SV aceitam só inteiro. As demais aceitam decimal com vírgula e até 3 casas. Sempre maior que
 *   zero e no máximo 9.999.999,999. Ponto não é aceito, para não confundir 1.500 com 1,5. A observação tem
 *   até 200 caracteres.
 * CLASSE INTERNA: Resultado (record) traz o valor (BigDecimal) e a mensagem de erro; erro nulo significa
 *   válido.
 * MÉTODOS:
 * - inteira(siglaUnidade): diz se a unidade só aceita número inteiro.
 * - dica(siglaUnidade): texto de ajuda mostrado ao usuário.
 * - validar(texto, siglaUnidade, obrigatoria): confere o texto digitado e devolve o valor ou o erro.
 * - formatar(valor): mostra o número com vírgula e sem zeros à direita (2.50 vira 2,5).
 */

import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Pattern;

public final class QuantidadeServico {

    public static final Set<String> UNIDADES_INTEIRAS = Set.of("ST", "ZCJ", "SV");
    public static final BigDecimal MAXIMO = new BigDecimal("9999999.999");
    public static final int MAX_OBSERVACAO = 200;
    private static final Pattern FORMATO = Pattern.compile("\\d+(,\\d+)?");

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

    public static String formatar(BigDecimal valor) {
        if (valor == null) return "";
        return valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
