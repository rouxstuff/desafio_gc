package gerdauGrupo16.desafiogc.service;

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
        assertFalse(EmailUsuario.valido("admin"));              // o login antigo, sem @
        assertFalse(EmailUsuario.valido("maria@empresa"));      // sem extensão
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
