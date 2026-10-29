package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
