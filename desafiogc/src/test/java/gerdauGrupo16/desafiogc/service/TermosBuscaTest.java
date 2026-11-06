package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: TermosBuscaTest.java
 * TIPO: classe de teste (JUnit) de TermosBusca.
 * MÉTODOS:
 * - separaPorPontoEVirgula(): divide nos pontos e vírgulas.
 * - tiraEspacosDasPontas(): remove espaços antes e depois de cada termo.
 * - mantemEspacosDentroDoTermo(): preserva espaços internos.
 * - ignoraTermosVazios(): descarta termos vazios.
 * - semTermoNenhumDevolveListaVazia(): nulo, branco e só separadores dão lista vazia.
 * - termosRepetidosContamUmaVezSemDiferenciarCaixaNemAcento(): remove repetidos.
 * - umSoTermoContinuaFuncionando(): um termo sem separador continua igual.
 * - limitaAoMaximoDeTermos(): vale no máximo MAX_TERMOS termos.
 */

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TermosBuscaTest {

    @Test
    void separaPorPontoEVirgula() {
        assertEquals(List.of("Ajudante", "Mecanico", "Almoço"), TermosBusca.separar("Ajudante;Mecanico;Almoço"));
    }

    @Test
    void tiraEspacosDasPontas() {
        assertEquals(List.of("Ajudante", "Mecanico"), TermosBusca.separar("  Ajudante ;  Mecanico  "));
    }

    @Test
    void mantemEspacosDentroDoTermo() {
        assertEquals(List.of("hora extra", "auxiliar de servicos"), TermosBusca.separar("hora extra;auxiliar de servicos"));
    }

    @Test
    void ignoraTermosVazios() {
        assertEquals(List.of("a", "b"), TermosBusca.separar(";a;;b;"));
        assertEquals(List.of("ajudante"), TermosBusca.separar("ajudante;"));
    }

    @Test
    void semTermoNenhumDevolveListaVazia() {
        assertTrue(TermosBusca.separar(null).isEmpty());
        assertTrue(TermosBusca.separar("   ").isEmpty());
        assertTrue(TermosBusca.separar(";;;").isEmpty());
    }

    @Test
    void termosRepetidosContamUmaVezSemDiferenciarCaixaNemAcento() {
        assertEquals(List.of("Almoço", "ajudante"), TermosBusca.separar("Almoço;ALMOCO;almoço ;ajudante"));
    }

    @Test
    void umSoTermoContinuaFuncionando() {
        assertEquals(List.of("eletricista"), TermosBusca.separar("eletricista"));
    }

    @Test
    void limitaAoMaximoDeTermos() {
        List<String> termos = TermosBusca.separar("a;b;c;d;e;f;g;h;i;j;k;l");
        assertEquals(TermosBusca.MAX_TERMOS, termos.size());
        assertEquals("a", termos.get(0));
        assertEquals("j", termos.get(termos.size() - 1));
    }
}
