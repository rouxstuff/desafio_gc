package gerdauGrupo16.desafiogc.model.unitys;

import gerdauGrupo16.desafiogc.model.entity.Servico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "unidade_medida")
@Getter @Setter
@NoArgsConstructor
public class UnidadeMedida {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Codigo")
    private String codigo;

    @Column(name = "Descricao")
    private String descricao;

    public UnidadeMedida(String codigo, String descricao){
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @OneToMany(mappedBy = "unidadeMedida")
    private List<Servico> servico;
}
