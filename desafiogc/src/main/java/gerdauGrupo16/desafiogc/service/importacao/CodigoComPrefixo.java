package gerdauGrupo16.desafiogc.service.importacao;

/**
 * Regra do código do serviço: PREFIXO-CODIGO (ex.: CIV-4057137). O código que já existe (SAP) é mantido
 * SEM alteração e só ganha o prefixo da categoria. Se já vier com o prefixo, nada muda (pode reimportar o arquivo).
 */
public final class CodigoComPrefixo {

    private CodigoComPrefixo() { }

    /** Maior número dos códigos no formato PREFIXO-NÚMERO (ex.: FAC-4048071 -> 4048071). 0 se não houver nenhum. */
    public static long maiorNumero(Iterable<String> codigos, String prefixo) {
        if (prefixo == null || prefixo.isBlank()) return 0;
        String cabeca = prefixo.strip().toUpperCase() + "-";
        long maior = 0;
        for (String codigo : codigos) {
            if (codigo == null || !codigo.toUpperCase().startsWith(cabeca)) continue;
            String resto = codigo.substring(cabeca.length());
            if (resto.isEmpty() || resto.length() > 18 || !resto.chars().allMatch(Character::isDigit)) continue;
            maior = Math.max(maior, Long.parseLong(resto));
        }
        return maior;
    }

    public static String aplicar(String codigo, String prefixo) {
        if (codigo == null) return "";
        String c = codigo.strip();
        if (c.isEmpty() || prefixo == null || prefixo.isBlank()) return c;
        String cabeca = prefixo.strip().toUpperCase() + "-";
        return c.toUpperCase().startsWith(cabeca) ? c : cabeca + c;
    }
}
