package gerdauGrupo16.desafiogc.model.entity;

/*
 * ARQUIVO: SolicitacaoCadastro.java
 * TIPO: entidade JPA (tabela solicitacao_cadastro). Getters e setters pelo Lombok.
 * FUNÇÃO: pedido de acesso feito por uma pessoa na tela pública de cadastro. Fica guardado até o
 *   administrador decidir; só na aprovação nasce o Usuario. A senha nunca é guardada em texto: já entra
 *   codificada em BCrypt e é copiada para o usuário na aprovação.
 * CAMPOS: id; nome (quem pede); email (login desejado, em minúsculas); senhaCodificada (BCrypt);
 *   perfilSolicitado (sempre COMPRADOR); status
 *   (StatusCadastro); solicitadoEm; decididoEm e decididoPor (e-mail do administrador); motivoRejeicao.
 */

import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.model.enumeration.StatusCadastro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_cadastro")
@Getter @Setter
@NoArgsConstructor
public class SolicitacaoCadastro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "senha_codificada", nullable = false)
    private String senhaCodificada;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_solicitado", nullable = false, length = 20)
    private Roles perfilSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCadastro status = StatusCadastro.PENDENTE;

    @Column(name = "solicitado_em", nullable = false)
    private LocalDateTime solicitadoEm;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    @Column(name = "decidido_por")
    private String decididoPor;

    @Column(name = "motivo_rejeicao", length = 200)
    private String motivoRejeicao;
}
