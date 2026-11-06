package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: ServicoRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade Servico (chave: código).
 * FUNÇÃO: todas as consultas de serviço. Os métodos terminados em StatusNot excluem um status (usado para
 *   esconder REJEITADO de quem não é administrador).
 * MÉTODOS:
 * - findByNomeServicoContaining(nomeServico): serviços cujo nome contém o texto.
 * - findByCategoria_CategoriaNomeAndNomeServicoContaining(categoriaNome, nomeServico): filtro por categoria
 *   e nome.
 * - findByCategoria_CategoriaNomeContaining(categoriaNome): filtro só por categoria.
 * - existsByCategoria_IdAndNomeServico(categoriaId, nomeServico): diz se o nome já existe na categoria
 *   (bloqueio de duplicata exata).
 * - findByCategoria_IdAndStatusIn(categoriaId, status): serviços de uma categoria em certos status.
 * - findByStatusIn(status): serviços em certos status.
 * - findByStatusOrderByNomeServico(status): serviços de um status, por nome (fila de aprovação).
 * - findByNomeServicoContainingOrCodigoServicoContaining(nome, codigo, pageable): página de serviços por
 *   nome ou código (administrador, vê todos).
 * - findByStatusNot(status, pageable): página sem um status.
 * - buscarPaginaSemStatus(status, termo, pageable): página por nome ou código sem um status (usuário
 *   comum).
 * - findByNomeServicoContainingAndStatusNot(nomeServico, status): por nome, sem um status.
 * - findByCategoria_CategoriaNomeAndNomeServicoContainingAndStatusNot(categoriaNome, nomeServico, status):
 *   por categoria e nome, sem um status.
 * - findByCategoria_CategoriaNomeContainingAndStatusNot(categoriaNome, status): por categoria, sem um
 *   status.
 * - findByStatusNot(status): todos sem um status.
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

    List<Servico> findByCategoria_IdAndStatusIn(Long categoriaId, Collection<StatusServico> status);

    List<Servico> findByStatusIn(Collection<StatusServico> status);

    List<Servico> findByStatusOrderByNomeServico(StatusServico status);

    Page<Servico> findByNomeServicoContainingOrCodigoServicoContaining(String nome, String codigo, Pageable pageable);

    Page<Servico> findByStatusNot(StatusServico status, Pageable pageable);

    @Query("select s from Servico s where s.status <> :status "
         + "and (s.nomeServico like concat('%', :termo, '%') or s.codigoServico like concat('%', :termo, '%'))")
    Page<Servico> buscarPaginaSemStatus(@Param("status") StatusServico status, @Param("termo") String termo, Pageable pageable);

    List<Servico> findByNomeServicoContainingAndStatusNot(String nomeServico, StatusServico status);

    List<Servico> findByCategoria_CategoriaNomeAndNomeServicoContainingAndStatusNot(String categoriaNome, String nomeServico, StatusServico status);

    List<Servico> findByCategoria_CategoriaNomeContainingAndStatusNot(String categoriaNome, StatusServico status);

    List<Servico> findByStatusNot(StatusServico status);

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
