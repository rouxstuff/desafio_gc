package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: UsuarioRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade Usuario.
 * MÉTODOS:
 * - findByEmailIgnoreCase(email): acha o usuário pelo e-mail, sem diferenciar maiúsculas (usado no login).
 * - existsByEmailIgnoreCase(email): diz se o e-mail já está em uso.
 * - findByComprador_Id(compradorId): usuário ligado a um comprador (há no máximo um).
 * - listarComComprador(): todos os usuários por e-mail, já com o comprador carregado (evita uma consulta
 *   por usuário).
 */

import gerdauGrupo16.desafiogc.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByComprador_Id(Long compradorId);

    @Query("select u from Usuario u left join fetch u.comprador order by u.email")
    List<Usuario> listarComComprador();
}
