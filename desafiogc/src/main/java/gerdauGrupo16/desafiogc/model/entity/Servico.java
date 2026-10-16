package gerdauGrupo16.desafiogc.model.entity;

import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="servico")
@Getter @Setter
@NoArgsConstructor
public class Servico {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, unique = true, length = 40)
    private String nomeServico;

    @ManyToOne
    @JoinColumn(name = "unidade_custo_id", nullable = true)
    private UnidadeCusto unidadeCusto;

    @ManyToOne
    @JoinColumn(name = "unidade_medida_id", nullable = true)
    private UnidadeMedida unidadeMedida;

    // dentro de Servico
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = true)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id", nullable = true)
    private Fornecedor fornecedor;

    public Servico(String codigo, String nomeServico) {
        this.codigo = codigo;
        this.nomeServico = nomeServico;
    }
}
