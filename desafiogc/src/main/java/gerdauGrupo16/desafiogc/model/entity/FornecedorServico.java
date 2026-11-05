package gerdauGrupo16.desafiogc.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [NOVO] Tabela de ligacao entre Fornecedor e Servico (muitos-para-muitos).
 *
 * Cada linha diz: "este fornecedor usa este servico".
 * Assim o servico existe UMA vez no catalogo e varios fornecedores podem se vincular a ele,
 * sem criar duplicatas.
 *
 * O unique (fornecedor_id, codigo_servico) impede o MESMO fornecedor de se vincular duas vezes
 * ao mesmo servico (aqui ele faz sentido, diferente de estar dentro da tabela servico).
 */
@Entity
@Table(name = "fornecedor_servico",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fornecedor_id", "codigo_servico"}))
@Getter
@Setter
@NoArgsConstructor
public class FornecedorServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_servico", nullable = false)
    private Servico servico;

    public FornecedorServico(Fornecedor fornecedor, Servico servico) {
        this.fornecedor = fornecedor;
        this.servico = servico;
    }
}
