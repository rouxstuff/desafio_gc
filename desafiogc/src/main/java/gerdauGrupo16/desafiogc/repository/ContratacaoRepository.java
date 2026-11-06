package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: ContratacaoRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade Contratacao.
 * MÉTODOS:
 * - findByUsuarioEmailIgnoreCaseOrderByCriadaEmDesc(email): contratações de um usuário, da mais nova para a
 *   mais antiga (tela Meus serviços).
 * - findTop200ByOrderByCriadaEmDesc(): as 200 contratações mais recentes de todos os usuários (visão do
 *   administrador).
 * - existsByUsuarioEmailIgnoreCaseAndCodigoServicoAndCriadaEmAfter(email, codigo, desde): diz se o mesmo
 *   usuário já contratou o mesmo serviço depois de um instante (evita registro duplicado quando a página é
 *   reenviada).
 */

import gerdauGrupo16.desafiogc.model.entity.Contratacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ContratacaoRepository extends JpaRepository<Contratacao, Long> {

    List<Contratacao> findByUsuarioEmailIgnoreCaseOrderByCriadaEmDesc(String usuarioEmail);

    List<Contratacao> findTop200ByOrderByCriadaEmDesc();

    boolean existsByUsuarioEmailIgnoreCaseAndCodigoServicoAndCriadaEmAfter(String usuarioEmail, String codigoServico,
                                                                          LocalDateTime desde);
}
