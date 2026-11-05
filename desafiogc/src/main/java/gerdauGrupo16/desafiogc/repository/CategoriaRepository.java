package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByCategoriaNomeContaining(String categoriaNome);

    // Incremento atômico: o banco trava a linha, então dois pedidos simultâneos nunca recebem o mesmo número.
    @Modifying
    @Query("update Categoria c set c.ultimoNumero = c.ultimoNumero + 1 where c.id = :id")
    int incrementarUltimoNumero(@Param("id") Long id);

    // Usado na importação: se vierem códigos no formato PREFIXO-000123, o contador não pode ficar atrás deles.
    @Modifying
    @Query("update Categoria c set c.ultimoNumero = :numero where c.id = :id and c.ultimoNumero < :numero")
    int garantirUltimoNumero(@Param("id") Long id, @Param("numero") long numero);

    @Query("select c.ultimoNumero from Categoria c where c.id = :id")
    Long buscarUltimoNumero(@Param("id") Long id);
}
