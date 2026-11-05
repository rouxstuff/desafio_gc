package gerdauGrupo16.desafiogc.repository;

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

    // [NOVO, do Kaio] busca só por categoria
    List<Servico> findByCategoria_CategoriaNomeContaining(String categoriaNome);

    // [NOVO, do Kaio] bloqueio de duplicata exata: mesmo nome na mesma categoria
    boolean existsByCategoria_IdAndNomeServico(Long categoriaId, String nomeServico);

    // [NOVO, do Kaio] base da régua de aderência (uma categoria)
    List<Servico> findByCategoria_IdAndStatusIn(Long categoriaId, Collection<StatusServico> status);

    // [NOVO] base da régua na tela de consulta, onde ainda não há categoria escolhida
    List<Servico> findByStatusIn(Collection<StatusServico> status);

    // fila de aprovação do administrador
    List<Servico> findByStatusOrderByNomeServico(StatusServico status);

    // catálogo paginado: busca por nome OU código (o chamador passa o mesmo termo nos dois)
    Page<Servico> findByNomeServicoContainingOrCodigoServicoContaining(String nome, String codigo, Pageable pageable);

    // catálogo para quem não é administrador: sem os serviços de um status (REJEITADO)
    Page<Servico> findByStatusNot(StatusServico status, Pageable pageable);

    @Query("select s from Servico s where s.status <> :status "
         + "and (s.nomeServico like concat('%', :termo, '%') or s.codigoServico like concat('%', :termo, '%'))")
    Page<Servico> buscarPaginaSemStatus(@Param("status") StatusServico status, @Param("termo") String termo, Pageable pageable);

    List<Servico> findByNomeServicoContainingAndStatusNot(String nomeServico, StatusServico status);

    List<Servico> findByCategoria_CategoriaNomeAndNomeServicoContainingAndStatusNot(String categoriaNome, String nomeServico, StatusServico status);

    List<Servico> findByCategoria_CategoriaNomeContainingAndStatusNot(String categoriaNome, StatusServico status);

    List<Servico> findByStatusNot(StatusServico status);

    // leve: 4 colunas de todos os serviços (índice em memória e validação da importação)
    @Query("select s.codigoServico as codigoServico, s.nomeServico as nomeServico, c.id as categoriaId, s.status as status "
         + "from Servico s left join s.categoria c")
    List<ServicoIndiceView> listarParaIndice();

    // [PERFORMANCE] listas suspensas: só código e nome, 1 query, sem joins nem entidades
    @Query("select s.codigoServico as codigoServico, s.nomeServico as nomeServico "
         + "from Servico s where s.status = :status order by s.nomeServico")
    List<ServicoOpcaoView> listarOpcoes(@Param("status") StatusServico status);

    // [BUSCA NOS VÍNCULOS] serviços do status informado cujo NOME ou CÓDIGO contém o termo (já em maiúsculas, sem acento).
    // Mesma projeção leve; o Pageable limita a quantidade de resultados.
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
