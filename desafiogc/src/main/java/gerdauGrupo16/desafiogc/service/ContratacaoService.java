package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: ContratacaoService.java
 * TIPO: classe de serviço (@Service).
 * FUNÇÃO: guarda e lista as contratações de serviços. Quando o comprador confirma um serviço
 *   APROVADO, a confirmação passa a ser gravada e fica ligada ao usuário (Meus serviços). Se o usuário está
 *   ligado a um comprador da base, o vínculo comprador x serviço também é criado (com a data). O administrador
 *   enxerga as contratações de todos. Reenviar a mesma confirmação em menos de 10 segundos não grava de novo.
 * MÉTODOS:
 * - registrar(usuarioEmail, servico, unidade, quantidade, adicionais, observacao): recusa serviço que não
 *   esteja APROVADO (409) e grava a contratação com
 *   os dados do momento (nome, unidade, quantidade, adicionais, observação) e cria o vínculo do comprador
 *   do usuário com o serviço, se ainda não existir.
 * - listar(usuarioEmail): contratações do usuário, da mais nova para a mais antiga.
 * - vincularCompradorDoUsuario(usuarioEmail, servico): cria o vínculo comprador x serviço do usuário, se ele
 *   tiver comprador ligado e o vínculo ainda não existir.
 * - listarTodas(): as 200 contratações mais recentes de todos (uso do administrador).
 */

import gerdauGrupo16.desafiogc.model.entity.Contratacao;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.model.entity.CompradorServico;
import gerdauGrupo16.desafiogc.repository.CompradorServicoRepository;
import gerdauGrupo16.desafiogc.repository.ContratacaoRepository;
import gerdauGrupo16.desafiogc.repository.UsuarioRepository;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContratacaoService {

    private static final long JANELA_DUPLICIDADE_SEGUNDOS = 10;

    private final ContratacaoRepository contratacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompradorServicoRepository compradorServicoRepository;

    public ContratacaoService(ContratacaoRepository contratacaoRepository,
                              UsuarioRepository usuarioRepository,
                              CompradorServicoRepository compradorServicoRepository) {
        this.contratacaoRepository = contratacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.compradorServicoRepository = compradorServicoRepository;
    }

    @Transactional
    public void registrar(String usuarioEmail, Servico servico, UnidadeMedida unidade, BigDecimal quantidade,
                          List<String> adicionais, String observacao) {
        if (servico.getStatus() != StatusServico.APROVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só serviço APROVADO pode ser contratado. Este está " + servico.getStatus() + ".");
        }
        LocalDateTime agora = LocalDateTime.now();
        boolean repetida = contratacaoRepository.existsByUsuarioEmailIgnoreCaseAndCodigoServicoAndCriadaEmAfter(
                usuarioEmail, servico.getCodigoServico(), agora.minusSeconds(JANELA_DUPLICIDADE_SEGUNDOS));
        if (repetida) {
            return;
        }
        Contratacao contratacao = new Contratacao();
        contratacao.setUsuarioEmail(usuarioEmail);
        contratacao.setCodigoServico(servico.getCodigoServico());
        contratacao.setNomeServico(servico.getNomeServico());
        contratacao.setUnidadeMedida(unidade == null ? null : unidade.getCodigo());
        contratacao.setQuantidade(quantidade);
        contratacao.setAdicionais(adicionais == null || adicionais.isEmpty() ? null : String.join(", ", adicionais));
        contratacao.setObservacao(observacao == null || observacao.isBlank() ? null : observacao);
        contratacao.setCriadaEm(agora);
        contratacaoRepository.save(contratacao);
        vincularCompradorDoUsuario(usuarioEmail, servico);
    }

    private void vincularCompradorDoUsuario(String usuarioEmail, Servico servico) {
        usuarioRepository.findByEmailIgnoreCase(usuarioEmail)
                .map(u -> u.getComprador())
                .ifPresent(comprador -> {
                    if (!compradorServicoRepository.existsByComprador_IdAndServico_CodigoServico(
                            comprador.getId(), servico.getCodigoServico())) {
                        compradorServicoRepository.save(new CompradorServico(comprador, servico));
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<Contratacao> listar(String usuarioEmail) {
        return contratacaoRepository.findByUsuarioEmailIgnoreCaseOrderByCriadaEmDesc(usuarioEmail);
    }

    @Transactional(readOnly = true)
    public List<Contratacao> listarTodas() {
        return contratacaoRepository.findTop200ByOrderByCriadaEmDesc();
    }
}
