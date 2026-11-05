package gerdauGrupo16.desafiogc.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "fornecedor")
@Getter
@Setter
@NoArgsConstructor
public class Fornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_fornecedor", unique = true, nullable = true)
    private String codigoFornecedor;

    @Column(name = "nome_fornecedor", nullable = true)
    private String nomeFornecedor;

    @JsonIgnore
    @OneToMany(mappedBy = "fornecedor")
    private List<FornecedorServico> vinculos;

    public Fornecedor(String codigoFornecedor) {
        this.codigoFornecedor = codigoFornecedor;
    }
}
