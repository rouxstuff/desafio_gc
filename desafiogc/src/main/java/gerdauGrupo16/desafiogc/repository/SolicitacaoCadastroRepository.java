package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: SolicitacaoCadastroRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade SolicitacaoCadastro.
 * MÉTODOS:
 * - findByStatusOrderBySolicitadoEmAsc(status): solicitações de um status, das mais antigas para as mais
 *   novas (a fila do administrador).
 * - existsByEmailIgnoreCaseAndStatus(email, status): diz se já há solicitação desse e-mail naquele status
 *   (usado para não aceitar dois pedidos pendentes do mesmo e-mail).
 */

import gerdauGrupo16.desafiogc.model.entity.SolicitacaoCadastro;
import gerdauGrupo16.desafiogc.model.enumeration.StatusCadastro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoCadastroRepository extends JpaRepository<SolicitacaoCadastro, Long> {

    List<SolicitacaoCadastro> findByStatusOrderBySolicitadoEmAsc(StatusCadastro status);

    boolean existsByEmailIgnoreCaseAndStatus(String email, StatusCadastro status);
}
