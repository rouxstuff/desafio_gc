package gerdauGrupo16.desafiogc.service.importacao;

/*
 * ARQUIVO: CodigoComPrefixo.java
 * TIPO: classe utilitária (final, só métodos estáticos).
 * FUNÇÃO: regra do código do serviço, PREFIXO-CÓDIGO (ex.: CIV-4057137). O código SAP existente é mantido e
 *   só ganha o prefixo da categoria. Se já vier com o prefixo, nada muda, então o arquivo pode ser
 *   reimportado.
 * MÉTODOS:
 * - maiorNumero(codigos, prefixo): maior número entre os códigos no formato PREFIXO-NÚMERO; 0 se não
 *   houver.
 * - aplicar(codigo, prefixo): devolve o código com o prefixo, sem duplicá-lo.
 */

public final class CodigoComPrefixo {

    private CodigoComPrefixo() { }

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
