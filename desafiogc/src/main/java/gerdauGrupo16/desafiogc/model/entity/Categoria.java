package gerdauGrupo16.desafiogc.model.entity;

/*
 * ARQUIVO: Categoria.java
 * TIPO: entidade JPA (tabela categoria). Getters, setters e construtor vazio gerados pelo Lombok.
 * FUNÇÃO: grupo de serviço (Civil, Eletromecânica, Facilities, Materiais, Insumos). Guarda o prefixo usado
 *   no código do serviço (CIV, ELETRO, FAC, MAT, INS) e o contador do último número, usado para gerar
 *   códigos novos sem repetir.
 * CAMPOS: id; categoriaNome (único, até 50); prefixo (único, até 10); ultimoNumero (contador, começa em 0).
 * MÉTODOS:
 * - Categoria(categoriaNome): cria só com o nome.
 * - Categoria(categoriaNome, prefixo): cria com nome e prefixo (usado pela importação).
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String categoriaNome;

    @Column(length = 10, unique = true)
    private String prefixo;

    @Column(name = "ultimo_numero", nullable = false,
            columnDefinition = "bigint default 0 not null")
    private long ultimoNumero = 0;

    public Categoria(String categoriaNome) { this.categoriaNome = categoriaNome; }

    public Categoria(String categoriaNome, String prefixo) {
        this.categoriaNome = categoriaNome;
        this.prefixo = prefixo;
    }
}
