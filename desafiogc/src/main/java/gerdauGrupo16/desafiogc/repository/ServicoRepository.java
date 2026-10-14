package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, String> {

    List<Servico> findByDescricaoContaining(String trecho);
}
