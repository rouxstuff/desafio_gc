package gerdauGrupo16.desafiogc.model.entity;

/*
 * ARQUIVO: FornecedorServico.java
 * TIPO: entidade JPA (tabela fornecedor_servico). Getters e setters pelo Lombok.
 * FUNÇÃO: vínculo entre um fornecedor e um serviço do catálogo. Um serviço pode ter vários fornecedores sem
 *   criar código duplicado. O par fornecedor + serviço é único no banco.
 * CAMPOS: id; fornecedor; servico (ambos carregados sob demanda, LAZY).
 * MÉTODOS:
 * - FornecedorServico(fornecedor, servico): cria o vínculo.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
