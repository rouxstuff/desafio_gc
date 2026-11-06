package gerdauGrupo16.desafiogc.model.entity;

/*
 * ARQUIVO: Contratacao.java
 * TIPO: entidade JPA (tabela contratacao). Getters e setters pelo Lombok.
 * FUNÇÃO: registro de que um usuário contratou um serviço APROVADO do catálogo. Guarda uma fotografia dos
 *   dados no momento da confirmação (nome do serviço, unidade, quantidade, adicionais e observação), assim o
 *   histórico não muda se o serviço for alterado depois. A ligação com o usuário é pelo e-mail de login e a
 *   ligação com o serviço é pelo código, sem chave estrangeira, para não atrapalhar importações e reuniões
 *   de códigos.
 * CAMPOS: id; usuarioEmail (quem contratou); codigoServico e nomeServico; unidadeMedida (sigla, como H ou
 *   ST); quantidade (pode ficar vazia quando o serviço aprovado ainda não tem unidade); adicionais (nomes dos
 *   adicionais marcados, separados por vírgula); observacao; criadaEm.
 * MÉTODOS:
 * - getQuantidadeFormatada(): texto para a tela, com vírgula e sem zeros à direita (2,5 H); traço quando não há
 *   quantidade.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratacao")
@Getter @Setter
@NoArgsConstructor
public class Contratacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_email", nullable = false, length = 254)
    private String usuarioEmail;

    @Column(name = "codigo_servico", nullable = false, length = 20)
    private String codigoServico;

    @Column(name = "nome_servico", nullable = false, length = 100)
    private String nomeServico;

    @Column(name = "unidade_medida", length = 10)
    private String unidadeMedida;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(length = 1000)
    private String adicionais;

    @Column(length = 200)
    private String observacao;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    public String getQuantidadeFormatada() {
        if (quantidade == null) {
            return "—";
        }
        String numero = quantidade.stripTrailingZeros().toPlainString().replace('.', ',');
        return unidadeMedida == null ? numero : numero + " " + unidadeMedida;
    }
}
