package gerdauGrupo16.desafiogc.service.reguaAderencia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Casos reais da base da Gerdau. Limites do sistema: >= 0.90 bloqueia, >= 0.60 sugere. */
class ReguaAderenciaTest {

    private final ReguaAderencia regua = new ReguaAderencia(new NormalizadorTexto());

    private void bloqueia(String a, String b) {
        double nota = regua.calcular(a, b);
        assertTrue(nota >= 0.90, "esperava bloquear '" + a + "' x '" + b + "' mas a nota foi " + nota);
    }

    private void naoBloqueia(String a, String b) {
        double nota = regua.calcular(a, b);
        assertTrue(nota < 0.90, "NÃO deveria bloquear '" + a + "' x '" + b + "' mas a nota foi " + nota);
    }

    @Test
    void mesmoNomeComSistemaEPontuacaoEhDuplicata() {
        bloqueia("SV-EQUIPE TELHADO", "EQUIPE TELHADO.");
        bloqueia("Armador - hora normal", "ARMADOR HORA NORMAL");
    }

    @Test
    void medidaEscritaDeOutroJeitoEhDuplicata() {
        assertEquals(1.0, regua.calcular("TUBO RIGIDO SOLDAVEL 25MM", "TUBO RIGIDO SOLDAVEL 25 MM"));
    }

    @Test
    void medidasDiferentesNuncaBloqueiamMasAindaSugerem() {
        naoBloqueia("TUBO RIGIDO SOLDAVEL 25MM", "TUBO RIGIDO SOLDAVEL 50MM");
        assertTrue(regua.calcular("TUBO RIGIDO SOLDAVEL 25MM", "TUBO RIGIDO SOLDAVEL 50MM") >= 0.60);
        naoBloqueia("AJUDANTE HE 50", "AJUDANTE HE 100");
    }

    @Test
    void horaExtraEHoraNormalSaoServicosDiferentes() {
        naoBloqueia("TECNICO SEGURANCA TRABALHO HORA NORMAL", "TECNICO SEGURANCA TRABALHO HORA EXTRA");
    }

    @Test
    void abreviacoesDoDicionarioViramDuplicata() {
        bloqueia("AJUDANTE - HORA EXTRA À DISPOSIÇÃO", "SV AJUDANTE HE DISP");
        bloqueia("ELETRICISTA PERICULOSIDADE", "ELETRICISTA PERIC");
        bloqueia("AUXILIAR DE SERVICOS GERAIS", "AUXILIAR SERVICOS GERAIS");
    }

    @Test
    void truncamentoEm40CaracteresBloqueia() {
        bloqueia("SV - FURAÇÃO EM CONCRETO COM COROA DIAM", "SV - FURAÇÃO EM CONCRETO COM COROA DIAMA");
    }

    @Test
    void palavraRealmenteDiferenteOuAMaisSoSugere() {
        naoBloqueia("PORTA EXT SIMPLES", "PORTA INT SIMPLES");
        naoBloqueia("MARTELETEIRO HE NOT", "MARTELETEIRO HE NOT RES");
    }

    @Test
    void maiorEMenorQueSaoServicosDiferentes() {
        naoBloqueia("MOBILIZACAO DE EMERGENCIA >3DIAS", "MOBILIZACAO DE EMERGENCIA <3DIAS");
    }

    @Test
    void nomesSemRelacaoTemNotaZero() {
        assertEquals(0.0, regua.calcular("AJUDANTE", "PEDREIRO"));
    }
}
