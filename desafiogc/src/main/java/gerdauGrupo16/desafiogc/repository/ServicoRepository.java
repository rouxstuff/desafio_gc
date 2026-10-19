package gerdauGrupo16.desafiogc.repository;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, String> {

    List<Servico> findByNomeServicoContaining(String nomeServico);

    List<Servico> findByCategoria_CategoriaNomeAndNomeServicoContaining(String categoriaNome, String nomeServico);

    List<Servico> findByCategoria_CategoriaNomeContaining(String categoriaNome);

    boolean existsByCategoria_IdAndNomeServico(Long categoriaId, String nomeServico);

    List<Servico> findByCategoria_IdAndStatusIn(Long categoriaId, Collection<StatusServico> status);


}