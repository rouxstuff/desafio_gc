package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: UnidadeCustoRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade UnidadeCusto.
 * FUNÇÃO: usa só os métodos prontos (findAll, findById, findAllById...).
 */

import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeCustoRepository extends JpaRepository<UnidadeCusto, Long> {
}
