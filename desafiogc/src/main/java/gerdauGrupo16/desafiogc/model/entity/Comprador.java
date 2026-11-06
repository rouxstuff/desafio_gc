package gerdauGrupo16.desafiogc.model.entity;

/*
 * ARQUIVO: Comprador.java
 * TIPO: entidade JPA (tabela fornecedor, nome antigo mantido no banco). Getters e setters pelo Lombok.
 * FUNÇÃO: empresa que atende serviços. Na base saneada o comprador tem só o código (anonimizado); o nome é
 *   opcional.
 * CAMPOS: id; codigoComprador (único); nomeComprador (opcional); vinculos (lista dos vínculos com
 *   serviços, ignorada no JSON).
 * MÉTODOS:
 * - Comprador(codigoComprador): cria um comprador só com o código (usado pela importação de vínculos).
 */

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
public class Comprador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_fornecedor", unique = true, nullable = true)
    private String codigoComprador;

    @Column(name = "nome_fornecedor", nullable = true)
    private String nomeComprador;

    @JsonIgnore
    @OneToMany(mappedBy = "comprador")
    private List<CompradorServico> vinculos;

    public Comprador(String codigoComprador) {
        this.codigoComprador = codigoComprador;
    }
}
