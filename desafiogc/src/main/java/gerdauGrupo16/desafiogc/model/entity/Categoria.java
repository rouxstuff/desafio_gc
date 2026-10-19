package gerdauGrupo16.desafiogc.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria_nome", nullable = false, unique = true, length = 50)
    private String categoriaNome;

    @JsonIgnore
    @OneToMany(mappedBy = "categoria")
    private List<Servico> servicos;

    public Categoria(String categoriaNome) { this.categoriaNome = categoriaNome; }
}