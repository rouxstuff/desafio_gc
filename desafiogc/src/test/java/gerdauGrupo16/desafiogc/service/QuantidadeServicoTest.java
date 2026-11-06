package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: QuantidadeServicoTest.java
 * TIPO: classe de teste (JUnit) de QuantidadeServico.
 * MÉTODOS:
 * - pecaAceitaSoInteiro(): ST, SV e ZCJ aceitam inteiro e recusam decimal.
 * - pesoEMedidaAceitamDecimalComVirgula(): KG, TO e M3 aceitam vírgula, até 3 casas.
 * - rejeitaPontoZeroNegativoELetras(): recusa ponto, zero, negativo, letras e valor acima do máximo.
 * - obrigatoriaEOpcional(): quantidade em branco só é erro quando obrigatória; sem unidade, quantidade
 *   informada é erro.
 * - formataComVirgula(): 10.000 vira 10 e 2.50 vira 2,5.
 */

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuantidadeServicoTest {

    @Test
    void pecaAceitaSoInteiro() {
        assertEquals(new BigDecimal("5"), QuantidadeServico.validar("5", "ST", true).valor());
        assertNotNull(QuantidadeServico.validar("2,5", "ST", true).erro());
        assertNotNull(QuantidadeServico.validar("3", "SV", true).valor());
        assertNotNull(QuantidadeServico.validar("1,5", "ZCJ", true).erro());
    }

    @Test
    void pesoEMedidaAceitamDecimalComVirgula() {
        assertEquals(new BigDecimal("2.5"), QuantidadeServico.validar("2,5", "KG", true).valor());
        assertEquals(new BigDecimal("0.125"), QuantidadeServico.validar("0,125", "TO", true).valor());
        assertNotNull(QuantidadeServico.validar("0,1234", "M3", true).erro());
    }

    @Test
    void rejeitaPontoZeroNegativoELetras() {
        assertNotNull(QuantidadeServico.validar("1.500", "KG", true).erro());
        assertNotNull(QuantidadeServico.validar("0", "KG", true).erro());
        assertNotNull(QuantidadeServico.validar("-3", "ST", true).erro());
        assertNotNull(QuantidadeServico.validar("abc", "ST", true).erro());
        assertNotNull(QuantidadeServico.validar("99999999", "ST", true).erro());
    }

    @Test
    void obrigatoriaEOpcional() {
        assertEquals("Informe a quantidade.", QuantidadeServico.validar("  ", "ST", true).erro());
        assertNull(QuantidadeServico.validar("", "ST", false).erro());
        assertNull(QuantidadeServico.validar(null, null, false).valor());
        assertNotNull(QuantidadeServico.validar("4", null, false).erro());
    }

    @Test
    void formataComVirgula() {
        assertEquals("10", QuantidadeServico.formatar(new BigDecimal("10.000")));
        assertEquals("2,5", QuantidadeServico.formatar(new BigDecimal("2.50")));
    }
}
