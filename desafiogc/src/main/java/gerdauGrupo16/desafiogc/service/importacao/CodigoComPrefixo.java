package gerdauGrupo16.desafiogc.service.importacao;

/**
 * Regra do código do serviço: PREFIXO-CODIGO (ex.: CIV-4057137). O código que já existe (SAP) é mantido
 * SEM alteração e só ganha o prefixo da categoria. Se já vier com o prefixo, nada muda (pode reimportar o arquivo).
 */
public final class CodigoComPrefixo {

    private CodigoComPrefixo() { }

    public static String aplicar(String codigo, String prefixo) {
        if (codigo == null) return "";
        String c = codigo.strip();
        if (c.isEmpty() || prefixo == null || prefixo.isBlank()) return c;
        String cabeca = prefixo.strip().toUpperCase() + "-";
        return c.toUpperCase().startsWith(cabeca) ? c : cabeca + c;
    }
}
