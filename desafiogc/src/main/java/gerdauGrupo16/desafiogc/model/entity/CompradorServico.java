package gerdauGrupo16.desafiogc.model.entity;

/*
 * ARQUIVO: CompradorServico.java
 * TIPO: entidade JPA (tabela fornecedor_servico, nome antigo mantido no banco). Getters e setters pelo Lombok.
 * FUNÇÃO: vínculo entre um comprador e um serviço do catálogo. Um serviço pode ter vários compradores sem
 *   criar código duplicado. O par comprador + serviço é único no banco.
 * CAMPOS: id; comprador; servico (ambos carregados sob demanda, LAZY); vinculadoEm (quando o vínculo foi
 *   criado; vazio nos vínculos antigos, que vieram da importação inicial).
 * MÉTODOS:
 * - CompradorServico(comprador, servico): cria o vínculo.
 * - aoCriar(): antes de gravar, preenche vinculadoEm com o momento atual se estiver vazio.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "fornecedor_servico",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fornecedor_id", "codigo_servico"}))
@Getter
@Setter
@NoArgsConstructor
public class CompradorServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Comprador comprador;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_servico", nullable = false)
    private Servico servico;

    @Column(name = "vinculado_em")
    private LocalDateTime vinculadoEm;

    public CompradorServico(Comprador comprador, Servico servico) {
        this.comprador = comprador;
        this.servico = servico;
    }

    @PrePersist
    protected void aoCriar() {
        if (vinculadoEm == null) {
            vinculadoEm = LocalDateTime.now();
        }
    }
}
