package gerdauGrupo16.desafiogc.model.entity;

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

    // O login agora é o E-MAIL (sempre em minúsculas). A coluna continua "usuario_nome" de propósito: assim bancos que
    // já existem não precisam de migração de esquema (o sistema converte os logins antigos na subida; ver CargaInicialUnidades).
    @Column(name = "usuario_nome", nullable = false, unique = true)
    private String email;

    @Column(name = "usuario_senha", nullable = false)
    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Roles> roles = new HashSet<>();

    // Fornecedor (já existente na base saneada) a que este usuário pertence. Só faz sentido para o perfil FORNECEDOR.
    // unique = true: UM usuário por fornecedor (o banco também garante; vários usuários sem fornecedor continuam valendo).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = true, unique = true)
    private Fornecedor fornecedor;
}
