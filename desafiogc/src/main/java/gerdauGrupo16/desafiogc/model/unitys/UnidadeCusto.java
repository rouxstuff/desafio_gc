package gerdauGrupo16.desafiogc.model.unitys;

/*
 * ARQUIVO: UnidadeCusto.java
 * TIPO: entidade JPA (tabela unidade_custo). Getters, setters e construtor vazio pelo Lombok.
 * FUNÇÃO: adicional de mão de obra que pode acompanhar um serviço (hora normal, hora extra, adicional
 *   noturno, periculosidade, insalubridade, posto mensal, mobilização, retroativo...).
 * CAMPOS: id; maoDeObra (nome do adicional); descricao (explicação); servico (serviços que usam, ignorado
 *   no JSON).
 * MÉTODOS:
 * - UnidadeCusto(maoDeObra, descricao): cria o adicional.
 */
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "mao_de_obra", nullable = false)
    private String maoDeObra;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    public UnidadeCusto(String maoDeObra, String descricao) {
        this.maoDeObra = maoDeObra;
        this.descricao = descricao;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "unidadeCusto")
    private List<Servico> servico;
}
