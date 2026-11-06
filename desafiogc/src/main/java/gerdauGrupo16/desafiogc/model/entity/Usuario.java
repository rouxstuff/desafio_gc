package gerdauGrupo16.desafiogc.model.entity;

/*
 * ARQUIVO: Usuario.java
 * TIPO: entidade JPA (tabela usuarios). Getters e setters pelo Lombok.
 * FUNÇÃO: pessoa que entra no sistema. O login é o e-mail, sempre em minúsculas; a coluna se chama
 *   usuario_nome por compatibilidade com bancos antigos. A senha é guardada em BCrypt.
 * CAMPOS: id; email (único); senha; roles (perfis, tabela usuario_roles); comprador (só para perfil
 *   COMPRADOR; único, ou seja, um usuário por comprador).
 */

import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_nome", nullable = false, unique = true)
    private String email;

    @Column(name = "usuario_senha", nullable = false)
    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Roles> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = true, unique = true)
    private Comprador comprador;
}
