package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: CategoriaRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade Categoria.
 * FUNÇÃO: acesso ao banco para categorias, além dos métodos prontos (findAll, findById, save...).
 * MÉTODOS:
 * - findByCategoriaNomeContaining(categoriaNome): categorias cujo nome contém o texto.
 * - incrementarUltimoNumero(id): soma 1 ao contador da categoria. É um UPDATE, então o banco trava a linha
 *   e dois pedidos ao mesmo tempo nunca recebem o mesmo número.
 * - garantirUltimoNumero(id, numero): sobe o contador para pelo menos esse número (nunca diminui). Usado
 *   depois de importar códigos já numerados.
 * - buscarUltimoNumero(id): lê o valor atual do contador.
 */

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByCategoriaNomeContaining(String categoriaNome);

    @Modifying
    @Query("update Categoria c set c.ultimoNumero = c.ultimoNumero + 1 where c.id = :id")
    int incrementarUltimoNumero(@Param("id") Long id);

    @Modifying
    @Query("update Categoria c set c.ultimoNumero = :numero where c.id = :id and c.ultimoNumero < :numero")
    int garantirUltimoNumero(@Param("id") Long id, @Param("numero") long numero);

    @Query("select c.ultimoNumero from Categoria c where c.id = :id")
    Long buscarUltimoNumero(@Param("id") Long id);
}
