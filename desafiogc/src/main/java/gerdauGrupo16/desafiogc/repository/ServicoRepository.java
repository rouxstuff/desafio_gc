package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: ServicoRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade Servico (chave: código).
 * FUNÇÃO: todas as consultas de serviço. Os métodos terminados em Status devolvem só os serviços daquele
 *   status (usado para mostrar só APROVADO a quem não é administrador); os terminados em StatusNot excluem
 *   um status.
 * MÉTODOS:
 * - findByNomeServicoContaining(nomeServico): serviços cujo nome contém o texto.
 * - findByCategoria_CategoriaNomeAndNomeServicoContaining(categoriaNome, nomeServico): filtro por categoria
 *   e nome.
 * - findByCategoria_CategoriaNomeContaining(categoriaNome): filtro só por categoria.
 * - existsByCategoria_IdAndNomeServico(categoriaId, nomeServico): diz se o nome já existe na categoria
 *   (bloqueio de duplicata exata).
 * - findFirstByNomeServicoIgnoreCaseAndStatusNot(nomeServico, status): acha um serviço com o mesmo nome em
 *   qualquer categoria, ignorando os do status informado (usado para barrar o mesmo nome em outra categoria).
 * - findByCategoria_IdAndStatusIn(categoriaId, status): serviços de uma categoria em certos status.
 * - findByStatusIn(status): serviços em certos status.
 * - findByStatusOrderByNomeServico(status): serviços de um status, por nome (fila de aprovação).
 * - findByNomeServicoContainingOrCodigoServicoContaining(nome, codigo, pageable): página de serviços por
 *   nome ou código (administrador, vê todos).
 * - findByStatus(status, pageable): página só de um status.
 * - buscarPaginaPorStatus(status, termo, pageable): página por nome ou código só de um status (usuário
 *   comum, que vê apenas APROVADO).
 * - findByNomeServicoContainingAndStatus(nomeServico, status): por nome, só de um status.
 * - findByCategoria_CategoriaNomeAndNomeServicoContainingAndStatus(categoriaNome, nomeServico, status):
 *   por categoria e nome, só de um status.
 * - findByCategoria_CategoriaNomeContainingAndStatus(categoriaNome, status): por categoria, só de um
 *   status.
 * - findByStatus(status): todos de um status.
 * - listarParaIndice(): código, nome, categoria e status de todos os serviços, de forma leve (índice em
 *   memória e importação).
 * - listarOpcoes(status): código e nome dos serviços de um status, por nome (listas suspensas).
 * - buscarOpcoes(status, termo, pageable): código e nome dos serviços de um status cujo nome ou código
 *   contém o termo, limitado pelo pageable.
 * - listarCodigos(): só os códigos de todos os serviços.
 */

import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, String> {

    List<Servico> findByNomeServicoContaining(String nomeServico);

    List<Servico> findByCategoria_CategoriaNomeAndNomeServicoContaining(String categoriaNome, String nomeServico);

    List<Servico> findByCategoria_CategoriaNomeContaining(String categoriaNome);

    boolean existsByCategoria_IdAndNomeServico(Long categoriaId, String nomeServico);

    java.util.Optional<Servico> findFirstByNomeServicoIgnoreCaseAndStatusNot(String nomeServico, StatusServico status);

    List<Servico> findByCategoria_IdAndStatusIn(Long categoriaId, Collection<StatusServico> status);

    List<Servico> findByStatusIn(Collection<StatusServico> status);

    List<Servico> findByStatusOrderByNomeServico(StatusServico status);

    Page<Servico> findByNomeServicoContainingOrCodigoServicoContaining(String nome, String codigo, Pageable pageable);

    Page<Servico> findByStatus(StatusServico status, Pageable pageable);

    @Query("select s from Servico s where s.status = :status "
         + "and (s.nomeServico like concat('%', :termo, '%') or s.codigoServico like concat('%', :termo, '%'))")
    Page<Servico> buscarPaginaPorStatus(@Param("status") StatusServico status, @Param("termo") String termo, Pageable pageable);

    List<Servico> findByNomeServicoContainingAndStatus(String nomeServico, StatusServico status);

    List<Servico> findByCategoria_CategoriaNomeAndNomeServicoContainingAndStatus(String categoriaNome, String nomeServico, StatusServico status);

    List<Servico> findByCategoria_CategoriaNomeContainingAndStatus(String categoriaNome, StatusServico status);

    List<Servico> findByStatus(StatusServico status);

    @Query("select s.codigoServico as codigoServico, s.nomeServico as nomeServico, c.id as categoriaId, s.status as status "
         + "from Servico s left join s.categoria c")
    List<ServicoIndiceView> listarParaIndice();

    @Query("select s.codigoServico as codigoServico, s.nomeServico as nomeServico "
         + "from Servico s where s.status = :status order by s.nomeServico")
    List<ServicoOpcaoView> listarOpcoes(@Param("status") StatusServico status);

    @Query("select s.codigoServico as codigoServico, s.nomeServico as nomeServico "
         + "from Servico s where s.status = :status "
         + "and (upper(s.nomeServico) like concat('%', :termo, '%') "
         + "or upper(s.codigoServico) like concat('%', :termo, '%')) "
         + "order by s.nomeServico")
    List<ServicoOpcaoView> buscarOpcoes(@Param("status") StatusServico status,
                                        @Param("termo") String termo,
                                        Pageable pageable);

    @Query("select s.codigoServico from Servico s")
    List<String> listarCodigos();
}
