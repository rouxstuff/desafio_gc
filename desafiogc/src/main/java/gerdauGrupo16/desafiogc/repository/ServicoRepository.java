package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, String> {

    List<Servico> findByNomeServicoContaining(String nomeServico);
    List<Servico> findByCategoria_CategoriaNomeAndNomeServicoContaining(String categoriaNome, String nomeServico);
}
