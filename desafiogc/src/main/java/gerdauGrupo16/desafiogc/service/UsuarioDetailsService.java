package gerdauGrupo16.desafiogc.service;

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
        // o "username" do formulário de login é o e-mail
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
