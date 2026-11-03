package gerdauGrupo16.desafiogc.model.entity;

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
    private String prefixo;                       // CIV, ELETRO, FAC

    @Column(name = "ultimo_numero", nullable = false,
            columnDefinition = "bigint default 0 not null")
    private long ultimoNumero = 0;

    public Categoria(String categoriaNome) { this.categoriaNome = categoriaNome; }

    public Categoria(String categoriaNome, String prefixo) {
        this.categoriaNome = categoriaNome;
        this.prefixo = prefixo;
    }
}