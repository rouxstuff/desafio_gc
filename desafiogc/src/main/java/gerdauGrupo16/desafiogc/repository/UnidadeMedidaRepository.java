package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: UnidadeMedidaRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade UnidadeMedida.
 * FUNÇÃO: usa só os métodos prontos (findAll, findById...).
 */

import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedida, Long> {
}
