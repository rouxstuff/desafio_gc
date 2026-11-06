package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: UsuarioService.java
 * TIPO: classe de serviço (@Service).
 * FUNÇÃO: gestão de usuários feita pelo administrador. Regras: e-mail válido e único; senha provisória de 8
 *   a 72 bytes guardada só em BCrypt; o perfil COMPRADOR pode (mas não precisa) ser ligado a um comprador
 *   da base; ADMIN não é ligado a comprador; cada comprador tem no máximo um usuário.
 * CLASSES INTERNAS: CompradorLinha (record) é uma linha da lista de compradores com a quantidade de
 *   usuários (rotulo() monta o texto código - nome); Painel (record) reúne usuários, compradores e
 *   compradores sem usuário.
 * MÉTODOS:
 * - painel(): lê tudo o que a tela do administrador precisa.
 * - criar(email, senha, perfil, compradorId): cria o usuário. 400 dado inválido, 404 comprador
 *   inexistente, 409 e-mail já usado ou comprador que já tem usuário.
 * - criarComSenhaCodificada(email, senhaCodificada, perfil, compradorId): mesmas regras, mas recebe a
 *   senha já em BCrypt; usado quando o administrador aprova uma solicitação de cadastro (CadastroService).
 * - gravar(emailNormalizado, senhaCodificada, perfil, compradorId): parte comum das duas criações.
 * - vincularComprador(usuarioId, compradorId): liga ou troca o comprador da base de um usuário de perfil
 *   COMPRADOR.
 * - garantirCompradorLivre(compradorId, usuarioIdPermitido): recusa (409) se outro usuário já usa o
 *   comprador.
 * - buscarComprador(id): acha o comprador ou 404.
 * - validarSenha(senha): confere tamanho mínimo e máximo (público, também usado no cadastro).
 * - erro(status, mensagem): cria o erro HTTP.
 */

import gerdauGrupo16.desafiogc.model.entity.Comprador;
import gerdauGrupo16.desafiogc.model.entity.Usuario;
import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.repository.CompradorRepository;
import gerdauGrupo16.desafiogc.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    public static final int SENHA_MINIMA = 8;
    public static final int SENHA_MAXIMA_BYTES = 72;

    public record CompradorLinha(Long id, String codigo, String nome, long usuarios) {
        public String rotulo() {
            String cod = codigo == null || codigo.isBlank() ? "(sem código)" : codigo;
            return nome == null || nome.isBlank() ? cod : cod + " - " + nome;
        }
    }

    public record Painel(List<Usuario> usuarios, List<CompradorLinha> compradores,
                         List<CompradorLinha> compradoresSemUsuario) { }

    private final UsuarioRepository usuarioRepository;
    private final CompradorRepository compradorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, CompradorRepository compradorRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.compradorRepository = compradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Painel painel() {
        List<Usuario> usuarios = usuarioRepository.listarComComprador();
        Map<Long, Long> usuariosPorComprador = new HashMap<>();
        for (Usuario u : usuarios) {
            if (u.getComprador() != null) usuariosPorComprador.merge(u.getComprador().getId(), 1L, Long::sum);
        }
        List<CompradorLinha> compradores = compradorRepository.findAll().stream()
                .sorted(Comparator.comparing(Comprador::getId))
                .map(f -> new CompradorLinha(f.getId(), f.getCodigoComprador(), f.getNomeComprador(),
                        usuariosPorComprador.getOrDefault(f.getId(), 0L)))
                .toList();
        List<CompradorLinha> semUsuario = compradores.stream().filter(f -> f.usuarios() == 0).toList();
        return new Painel(usuarios, compradores, semUsuario);
    }

    @Transactional
    public Usuario criar(String email, String senha, Roles perfil, Long compradorId) {
        String emailNormalizado = EmailUsuario.normalizar(email);
        if (!EmailUsuario.valido(emailNormalizado)) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe um e-mail válido (ex.: nome@empresa.com).");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw erro(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + emailNormalizado + ".");
        }
        validarSenha(senha);
        return gravar(emailNormalizado, passwordEncoder.encode(senha), perfil, compradorId);
    }

    @Transactional
    public Usuario criarComSenhaCodificada(String email, String senhaCodificada, Roles perfil, Long compradorId) {
        String emailNormalizado = EmailUsuario.normalizar(email);
        if (!EmailUsuario.valido(emailNormalizado)) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe um e-mail válido (ex.: nome@empresa.com).");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw erro(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + emailNormalizado + ".");
        }
        return gravar(emailNormalizado, senhaCodificada, perfil, compradorId);
    }

    private Usuario gravar(String emailNormalizado, String senhaCodificada, Roles perfil, Long compradorId) {
        if (perfil == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Escolha o perfil do usuário.");
        }

        Comprador comprador = null;
        if (compradorId != null) {
            if (perfil != Roles.COMPRADOR) {
                throw erro(HttpStatus.BAD_REQUEST, "Só usuários com perfil Comprador podem ser ligados a um comprador.");
            }
            comprador = buscarComprador(compradorId);
            garantirCompradorLivre(comprador.getId(), null);
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(senhaCodificada);
        usuario.getRoles().add(perfil);
        usuario.setComprador(comprador);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario vincularComprador(Long usuarioId, Long compradorId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (!usuario.getRoles().contains(Roles.COMPRADOR)) {
            throw erro(HttpStatus.CONFLICT, "Só usuários com perfil Comprador podem ser ligados a um comprador.");
        }
        if (compradorId == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Escolha o comprador.");
        }
        Comprador comprador = buscarComprador(compradorId);
        garantirCompradorLivre(comprador.getId(), usuario.getId());
        usuario.setComprador(comprador);
        return usuario;
    }

    private void garantirCompradorLivre(Long compradorId, Long usuarioIdPermitido) {
        usuarioRepository.findByComprador_Id(compradorId)
                .filter(u -> !u.getId().equals(usuarioIdPermitido))
                .ifPresent(u -> {
                    throw erro(HttpStatus.CONFLICT, "Este comprador já tem um usuário: " + u.getEmail() + ".");
                });
    }

    private Comprador buscarComprador(Long id) {
        return compradorRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Comprador não encontrado."));
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < SENHA_MINIMA) {
            throw erro(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos " + SENHA_MINIMA + " caracteres.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > SENHA_MAXIMA_BYTES) {
            throw erro(HttpStatus.BAD_REQUEST, "A senha é grande demais (máximo de " + SENHA_MAXIMA_BYTES + " bytes).");
        }
    }

    private static ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
