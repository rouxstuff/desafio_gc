package gerdauGrupo16.desafiogc.service.importacao;

/*
 * ARQUIVO: CodigoComPrefixoTest.java
 * TIPO: classe de teste (JUnit) de CodigoComPrefixo.
 * MÉTODOS:
 * - codigoExistenteGanhaOPrefixoSemMudar(): o código SAP só ganha o prefixo.
 * - jaComPrefixoNaoDuplica(): código que já tem o prefixo não muda.
 * - semPrefixoNaCategoriaDeixaComoEsta(): sem prefixo, o código fica como veio; nulo vira vazio.
 * - maiorNumeroDaCategoriaIgnoraOutrosPrefixosEFormatosInvalidos(): acha o maior número da categoria,
 *   ignorando outros prefixos e formatos inválidos.
 */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodigoComPrefixoTest {

    @Test
    void codigoExistenteGanhaOPrefixoSemMudar() {
        assertEquals("CIV-4057137", CodigoComPrefixo.aplicar("4057137", "CIV"));
        assertEquals("ELETRO-4033776", CodigoComPrefixo.aplicar(" 4033776 ", "ELETRO"));
    }

    @Test
    void jaComPrefixoNaoDuplica() {
        assertEquals("FAC-4048071", CodigoComPrefixo.aplicar("FAC-4048071", "FAC"));
        assertEquals("fac-000001", CodigoComPrefixo.aplicar("fac-000001", "FAC"));
    }

    @Test
    void semPrefixoNaCategoriaDeixaComoEsta() {
        assertEquals("4057137", CodigoComPrefixo.aplicar("4057137", null));
        assertEquals("4057137", CodigoComPrefixo.aplicar("4057137", " "));
        assertEquals("", CodigoComPrefixo.aplicar(null, "CIV"));
    }

    @Test
    void maiorNumeroDaCategoriaIgnoraOutrosPrefixosEFormatosInvalidos() {
        var codigos = java.util.List.of("FAC-4048071", "FAC-000007", "CIV-4999999", "FAC-ABC", "FAC-", "FACX-5000000", "fac-4048075");
        assertEquals(4048075L, CodigoComPrefixo.maiorNumero(codigos, "FAC"));
        assertEquals(4999999L, CodigoComPrefixo.maiorNumero(codigos, "CIV"));
        assertEquals(0L, CodigoComPrefixo.maiorNumero(codigos, "ELETRO"));
    }
}
