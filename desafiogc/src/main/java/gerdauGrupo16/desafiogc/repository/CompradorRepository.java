package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: CompradorRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade Comprador.
 * MÉTODOS:
 * - findByCodigoComprador(codigoComprador): compradores com esse código.
 */

import gerdauGrupo16.desafiogc.model.entity.Comprador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompradorRepository extends JpaRepository<Comprador, Long> {
    List<Comprador> findByCodigoComprador(String codigoComprador);

}
