package gerdauGrupo16.desafiogc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="servico")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, unique = true, length = 40)
    private String descricao;
}
