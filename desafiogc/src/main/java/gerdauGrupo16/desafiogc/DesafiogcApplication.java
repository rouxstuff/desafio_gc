package gerdauGrupo16.desafiogc;

/*
 * ARQUIVO: DesafiogcApplication.java
 * TIPO: classe principal (@SpringBootApplication).
 * FUNÇÃO: ponto de entrada do sistema. Ao executar o método main o Spring Boot sobe o servidor web (porta
 *   8080), conecta ao banco PostgreSQL e registra todos os controllers, services e repositórios do pacote
 *   gerdauGrupo16.desafiogc.
 * MÉTODOS:
 * - main(args): inicia a aplicação (SpringApplication.run).
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DesafiogcApplication {

	public static void main(String[] args) {

		SpringApplication.run(DesafiogcApplication.class, args);
	}

}
