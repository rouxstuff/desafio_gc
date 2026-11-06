package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: UsuarioDetailsService.java
 * TIPO: classe de serviço (@Service) que implementa UserDetailsService do Spring Security.
 * FUNÇÃO: ensina o Spring Security a buscar o usuário no banco durante o login. O nome digitado no
 *   formulário é o e-mail.
 * MÉTODOS:
 * - loadUserByUsername(email): normaliza o e-mail, procura o usuário, e devolve login, senha em BCrypt e
 *   perfis. Se não existir lança UsernameNotFoundException.
 */

import gerdauGrupo16.desafiogc.model.entity.Usuario;
import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        String emailNormalizado = EmailUsuario.normalizar(email);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado: " + emailNormalizado));

        String[] roles = usuario.getRoles().stream()
                .map(Roles::name)
                .toArray(String[]::new);

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(roles)
                .build();
    }

}
