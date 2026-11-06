package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: UsuarioRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade Usuario.
 * MÉTODOS:
 * - findByEmailIgnoreCase(email): acha o usuário pelo e-mail, sem diferenciar maiúsculas (usado no login).
 * - existsByEmailIgnoreCase(email): diz se o e-mail já está em uso.
 * - findByFornecedor_Id(fornecedorId): usuário ligado a um fornecedor (há no máximo um).
 * - listarComFornecedor(): todos os usuários por e-mail, já com o fornecedor carregado (evita uma consulta
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

    Optional<Usuario> findByFornecedor_Id(Long fornecedorId);

    @Query("select u from Usuario u left join fetch u.fornecedor order by u.email")
    List<Usuario> listarComFornecedor();
}
