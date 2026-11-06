package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: UsuarioService.java
 * TIPO: classe de serviço (@Service).
 * FUNÇÃO: gestão de usuários feita pelo administrador. Regras: e-mail válido e único; senha provisória de 8
 *   a 72 bytes guardada só em BCrypt; perfil FORNECEDOR exige um fornecedor; ADMIN e COMPRADOR não têm
 *   fornecedor; cada fornecedor tem no máximo um usuário.
 * CLASSES INTERNAS: FornecedorLinha (record) é uma linha da lista de fornecedores com a quantidade de
 *   usuários (rotulo() monta o texto código - nome); Painel (record) reúne usuários, fornecedores e
 *   fornecedores sem usuário.
 * MÉTODOS:
 * - painel(): lê tudo o que a tela do administrador precisa.
 * - criar(email, senha, perfil, fornecedorId): cria o usuário. 400 dado inválido, 404 fornecedor
 *   inexistente, 409 e-mail já usado ou fornecedor que já tem usuário.
 * - vincularFornecedor(usuarioId, fornecedorId): liga ou troca o fornecedor de um usuário de perfil
 *   FORNECEDOR.
 * - garantirFornecedorLivre(fornecedorId, usuarioIdPermitido): recusa (409) se outro usuário já usa o
 *   fornecedor.
 * - buscarFornecedor(id): acha o fornecedor ou 404.
 * - validarSenha(senha): confere tamanho mínimo e máximo.
 * - erro(status, mensagem): cria o erro HTTP.
 */

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Usuario;
import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.repository.FornecedorRepository;
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

    public record FornecedorLinha(Long id, String codigo, String nome, long usuarios) {
        public String rotulo() {
            String cod = codigo == null || codigo.isBlank() ? "(sem código)" : codigo;
            return nome == null || nome.isBlank() ? cod : cod + " - " + nome;
        }
    }

    public record Painel(List<Usuario> usuarios, List<FornecedorLinha> fornecedores,
                         List<FornecedorLinha> fornecedoresSemUsuario) { }

    private final UsuarioRepository usuarioRepository;
    private final FornecedorRepository fornecedorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, FornecedorRepository fornecedorRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Painel painel() {
        List<Usuario> usuarios = usuarioRepository.listarComFornecedor();
        Map<Long, Long> usuariosPorFornecedor = new HashMap<>();
        for (Usuario u : usuarios) {
            if (u.getFornecedor() != null) usuariosPorFornecedor.merge(u.getFornecedor().getId(), 1L, Long::sum);
        }
        List<FornecedorLinha> fornecedores = fornecedorRepository.findAll().stream()
                .sorted(Comparator.comparing(Fornecedor::getId))
                .map(f -> new FornecedorLinha(f.getId(), f.getCodigoFornecedor(), f.getNomeFornecedor(),
                        usuariosPorFornecedor.getOrDefault(f.getId(), 0L)))
                .toList();
        List<FornecedorLinha> semUsuario = fornecedores.stream().filter(f -> f.usuarios() == 0).toList();
        return new Painel(usuarios, fornecedores, semUsuario);
    }

    @Transactional
    public Usuario criar(String email, String senha, Roles perfil, Long fornecedorId) {
        String emailNormalizado = EmailUsuario.normalizar(email);
        if (!EmailUsuario.valido(emailNormalizado)) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe um e-mail válido (ex.: nome@empresa.com).");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw erro(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + emailNormalizado + ".");
        }
        validarSenha(senha);
        if (perfil == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Escolha o perfil do usuário.");
        }

        Fornecedor fornecedor = null;
        if (perfil == Roles.FORNECEDOR) {
            if (fornecedorId == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Usuário com perfil Fornecedor precisa estar vinculado a um fornecedor.");
            }
            fornecedor = buscarFornecedor(fornecedorId);
            garantirFornecedorLivre(fornecedor.getId(), null);
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.getRoles().add(perfil);
        usuario.setFornecedor(fornecedor);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario vincularFornecedor(Long usuarioId, Long fornecedorId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (!usuario.getRoles().contains(Roles.FORNECEDOR)) {
            throw erro(HttpStatus.CONFLICT, "Só usuários com perfil Fornecedor podem ser vinculados a um fornecedor.");
        }
        if (fornecedorId == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Escolha o fornecedor.");
        }
        Fornecedor fornecedor = buscarFornecedor(fornecedorId);
        garantirFornecedorLivre(fornecedor.getId(), usuario.getId());
        usuario.setFornecedor(fornecedor);
        return usuario;
    }

    private void garantirFornecedorLivre(Long fornecedorId, Long usuarioIdPermitido) {
        usuarioRepository.findByFornecedor_Id(fornecedorId)
                .filter(u -> !u.getId().equals(usuarioIdPermitido))
                .ifPresent(u -> {
                    throw erro(HttpStatus.CONFLICT, "Este fornecedor já tem um usuário: " + u.getEmail() + ".");
                });
    }

    private Fornecedor buscarFornecedor(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Fornecedor não encontrado."));
    }

    private static void validarSenha(String senha) {
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
