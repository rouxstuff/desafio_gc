package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByCategoriaNomeContaining(String categoriaNome);
}
