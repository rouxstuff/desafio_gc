package gerdauGrupo16.desafiogc;

/*
 * ARQUIVO: DesafiogcApplicationTests.java
 * TIPO: classe de teste (JUnit, @SpringBootTest).
 * FUNÇÃO: teste de carga do contexto. Sobe a aplicação inteira, então precisa de banco acessível (DB_URL,
 *   DB_USER, DB_PASSWORD).
 * MÉTODOS:
 * - contextLoads(): passa se o Spring consegue iniciar sem erro.
 */

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DesafiogcApplicationTests {

	@Test
	void contextLoads() {
	}

}
