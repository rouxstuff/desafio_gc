package gerdauGrupo16.desafiogc.model.entity;

import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="servico",  uniqueConstraints = @UniqueConstraint(columnNames = {"categoria_id", "nome_servico"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    @Id
    @Column(name = "codigo_servico", length = 20)
    private String codigoServico;

    @Column(name = "nome_servico", nullable = false, length = 40)
    private String nomeServico;

    @Column(name = "descricao_servico", nullable = true, length = 100)
    private String descricaoServico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusServico status = StatusServico.PENDENTE;

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

    @Column(name = "solicitado_por")
    private String solicitadoPor;

    @Column(name = "motivo_rejeicao", length = 200)
    private String motivoRejeicao;

    public Servico(String codigoServico, String nomeServico) {
        this.codigoServico = codigoServico;
        this.nomeServico = nomeServico;
    }
}
