package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: EmailUsuarioTest.java
 * TIPO: classe de teste (JUnit) de EmailUsuario.
 * MÉTODOS:
 * - normalizaCaixaEEspacos(): minúsculas, sem espaços nas pontas e nulo vira vazio.
 * - aceitaEmailsComuns(): aceita e-mails comuns, com ponto, sinal de mais e domínio .local.
 * - recusaEmailsInvalidos(): recusa vazio, nulo, sem domínio, sem nome, com dois @ e com espaço.
 * - recusaEmailGrandeDemais(): recusa e-mail acima de 254 caracteres.
 */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailUsuarioTest {

    @Test
    void normalizaCaixaEEspacos() {
        assertEquals("maria@empresa.com", EmailUsuario.normalizar("  Maria@Empresa.COM "));
        assertEquals("", EmailUsuario.normalizar(null));
    }

    @Test
    void aceitaEmailsComuns() {
        assertTrue(EmailUsuario.valido("maria@empresa.com"));
        assertTrue(EmailUsuario.valido("maria.silva+compras@empresa.com.br"));
        assertTrue(EmailUsuario.valido("admin@gerdau.local"));
    }

    @Test
    void recusaEmailsInvalidos() {
        assertFalse(EmailUsuario.valido(""));
        assertFalse(EmailUsuario.valido(null));
        assertFalse(EmailUsuario.valido("admin"));
        assertFalse(EmailUsuario.valido("maria@empresa"));
        assertFalse(EmailUsuario.valido("@empresa.com"));
        assertFalse(EmailUsuario.valido("maria@@empresa.com"));
        assertFalse(EmailUsuario.valido("maria silva@empresa.com"));
    }

    @Test
    void recusaEmailGrandeDemais() {
        String grande = "a".repeat(250) + "@b.com";
        assertFalse(EmailUsuario.valido(grande));
    }
}
