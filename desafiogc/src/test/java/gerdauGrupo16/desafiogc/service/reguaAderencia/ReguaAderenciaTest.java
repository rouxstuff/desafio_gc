package gerdauGrupo16.desafiogc.service.reguaAderencia;

/*
 * ARQUIVO: ReguaAderenciaTest.java
 * TIPO: classe de teste (JUnit) de ReguaAderencia, com o dicionário real (regras.csv).
 * MÉTODOS:
 * - bloqueia(a, b): auxiliar; exige nota de 0,90 ou mais.
 * - naoBloqueia(a, b): auxiliar; exige nota abaixo de 0,90.
 * - mesmoNomeComSistemaEPontuacaoEhDuplicata(): prefixo SV e pontuação não diferenciam nomes.
 * - medidaEscritaDeOutroJeitoEhDuplicata(): 25MM e 25 MM têm nota 1.
 * - medidasDiferentesNuncaBloqueiamMasAindaSugerem(): 25MM e 50MM não bloqueiam, mas passam de 0,60.
 * - horaExtraEHoraNormalSaoServicosDiferentes(): HE e HN não bloqueiam.
 * - abreviacoesDoDicionarioViramDuplicata(): HE, DISP e PERIC do dicionário bloqueiam.
 * - truncamentoEm40CaracteresBloqueia(): nome cortado bloqueia.
 * - palavraRealmenteDiferenteOuAMaisSoSugere(): EXT x INT e palavra a mais não bloqueiam.
 * - maiorEMenorQueSaoServicosDiferentes(): maior e menor que não bloqueiam.
 * - nomesSemRelacaoTemNotaZero(): nomes sem relação dão 0.
 */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
