package gerdauGrupo16.desafiogc.service.importacao;

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
}
