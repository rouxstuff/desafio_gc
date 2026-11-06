package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: CadastroService.java
 * TIPO: classe de serviço (@Service).
 * FUNÇÃO: fluxo de cadastro com aprovação. A pessoa pede acesso na tela pública; o pedido fica PENDENTE e
 *   ela ainda não entra. O administrador aprova (nasce o usuário, já com a senha que a pessoa escolheu) ou
 *   reprova com motivo. Regras do pedido: nome obrigatório (até 100 caracteres); e-mail válido; perfil
 *   COMPRADOR (nunca ADMIN); senha de 8 a 72 bytes e igual à confirmação; e-mail que ainda
 *   não é usuário e não tem outro pedido pendente. Na aprovação o administrador pode ligar o usuário a um
 *   comprador da base (opcional).
 * MÉTODOS:
 * - solicitar(nome, email, senha, confirmacao, perfil): valida e guarda o pedido como PENDENTE. 400 dado
 *   inválido, 409 e-mail já usado ou já em análise.
 * - listarPendentes(): fila de pedidos PENDENTES, do mais antigo ao mais novo.
 * - aprovar(id, perfil, compradorId, administrador): cria o usuário e marca o pedido APROVADO. Se o
 *   perfil vier vazio vale o que a pessoa pediu.
 * - reprovar(id, motivo, administrador): marca REJEITADO; o motivo é obrigatório (até 200 caracteres).
 * - buscarPendente(id): acha o pedido ou 404; 409 se ele já foi decidido.
 * - erro(status, mensagem): cria o erro HTTP.
 */

import gerdauGrupo16.desafiogc.model.entity.SolicitacaoCadastro;
import gerdauGrupo16.desafiogc.model.enumeration.Roles;
import gerdauGrupo16.desafiogc.model.enumeration.StatusCadastro;
import gerdauGrupo16.desafiogc.repository.SolicitacaoCadastroRepository;
import gerdauGrupo16.desafiogc.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CadastroService {

    public static final int NOME_MAXIMO = 100;
    public static final int MOTIVO_MAXIMO = 200;

    private final SolicitacaoCadastroRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(SolicitacaoCadastroRepository solicitacaoRepository,
                           UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService,
                           PasswordEncoder passwordEncoder) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SolicitacaoCadastro solicitar(String nome, String email, String senha, String confirmacao, Roles perfil) {
        String nomeLimpo = nome == null ? "" : nome.strip();
        if (nomeLimpo.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o seu nome.");
        }
        if (nomeLimpo.length() > NOME_MAXIMO) {
            throw erro(HttpStatus.BAD_REQUEST, "O nome pode ter no máximo " + NOME_MAXIMO + " caracteres.");
        }
        String emailNormalizado = EmailUsuario.normalizar(email);
        if (!EmailUsuario.valido(emailNormalizado)) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe um e-mail válido (ex.: nome@empresa.com).");
        }
        if (perfil != Roles.COMPRADOR) {
            throw erro(HttpStatus.BAD_REQUEST, "O cadastro é para o perfil Comprador.");
        }
        UsuarioService.validarSenha(senha);
        if (!senha.equals(confirmacao)) {
            throw erro(HttpStatus.BAD_REQUEST, "As senhas não são iguais.");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw erro(HttpStatus.CONFLICT, "Este e-mail já tem acesso ao sistema. Entre com a sua senha ou fale com o administrador.");
        }
        if (solicitacaoRepository.existsByEmailIgnoreCaseAndStatus(emailNormalizado, StatusCadastro.PENDENTE)) {
            throw erro(HttpStatus.CONFLICT, "Já existe uma solicitação em análise para este e-mail. Aguarde a resposta do administrador.");
        }

        SolicitacaoCadastro solicitacao = new SolicitacaoCadastro();
        solicitacao.setNome(nomeLimpo);
        solicitacao.setEmail(emailNormalizado);
        solicitacao.setSenhaCodificada(passwordEncoder.encode(senha));
        solicitacao.setPerfilSolicitado(perfil);
        solicitacao.setStatus(StatusCadastro.PENDENTE);
        solicitacao.setSolicitadoEm(LocalDateTime.now());
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCadastro> listarPendentes() {
        return solicitacaoRepository.findByStatusOrderBySolicitadoEmAsc(StatusCadastro.PENDENTE);
    }

    @Transactional
    public SolicitacaoCadastro aprovar(Long id, Roles perfil, Long compradorId, String administrador) {
        SolicitacaoCadastro solicitacao = buscarPendente(id);
        Roles perfilFinal = perfil != null ? perfil : solicitacao.getPerfilSolicitado();
        usuarioService.criarComSenhaCodificada(solicitacao.getEmail(), solicitacao.getSenhaCodificada(),
                perfilFinal, compradorId);
        solicitacao.setStatus(StatusCadastro.APROVADO);
        solicitacao.setDecididoEm(LocalDateTime.now());
        solicitacao.setDecididoPor(administrador);
        return solicitacao;
    }

    @Transactional
    public SolicitacaoCadastro reprovar(Long id, String motivo, String administrador) {
        String motivoLimpo = motivo == null ? "" : motivo.strip();
        if (motivoLimpo.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o motivo da reprovação.");
        }
        if (motivoLimpo.length() > MOTIVO_MAXIMO) {
            throw erro(HttpStatus.BAD_REQUEST, "O motivo pode ter no máximo " + MOTIVO_MAXIMO + " caracteres.");
        }
        SolicitacaoCadastro solicitacao = buscarPendente(id);
        solicitacao.setStatus(StatusCadastro.REJEITADO);
        solicitacao.setMotivoRejeicao(motivoLimpo);
        solicitacao.setDecididoEm(LocalDateTime.now());
        solicitacao.setDecididoPor(administrador);
        return solicitacao;
    }

    private SolicitacaoCadastro buscarPendente(Long id) {
        SolicitacaoCadastro solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Solicitação não encontrada."));
        if (solicitacao.getStatus() != StatusCadastro.PENDENTE) {
            throw erro(HttpStatus.CONFLICT, "Esta solicitação já foi decidida.");
        }
        return solicitacao;
    }

    private static ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
