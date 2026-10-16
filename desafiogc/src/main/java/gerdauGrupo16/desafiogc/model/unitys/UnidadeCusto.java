package gerdauGrupo16.desafiogc.model.unitys;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "unidade_custo")
@Getter @Setter
@NoArgsConstructor
public class UnidadeCusto {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MaoDeObra", nullable = false)
    private String maoDeObra;

    @Column(name = "Descricao", nullable = false)
    private String descricao;

    public UnidadeCusto(String maoDeObra, String descricao) {
        this.maoDeObra = maoDeObra;
        this.descricao = descricao;
    }

    @OneToMany(mappedBy = "unidadeCusto")
    private List<Servico> servico;
}
