package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Login por e-mail (sem diferenciar maiúsculas de minúsculas). */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** O usuário ligado a um fornecedor (no máximo um). */
    Optional<Usuario> findByFornecedor_Id(Long fornecedorId);

    /** Tela do administrador: usuários já com o fornecedor carregado (evita uma consulta por linha). */
    @Query("select u from Usuario u left join fetch u.fornecedor order by u.email")
    List<Usuario> listarComFornecedor();
}
