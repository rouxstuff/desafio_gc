package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.controller.dto.SugestaoDTO;
import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.repository.CategoriaRepository;
import gerdauGrupo16.desafiogc.repository.ServicoOpcaoView;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import gerdauGrupo16.desafiogc.repository.UnidadeCustoRepository;
import gerdauGrupo16.desafiogc.repository.UnidadeMedidaRepository;
import gerdauGrupo16.desafiogc.service.importacao.CodigoComPrefixo;
import gerdauGrupo16.desafiogc.service.reguaAderencia.ReguaAderencia;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {
    public static final int LIMITE_SAP = 40;

    // [NOVO, do Kaio] limites da régua de aderência
    public static final double LIMITE_BLOQUEIO = 0.90;   // >= 90%: duplicado, bloqueia (409)
    public static final double LIMITE_SUGESTAO = 0.60;   // >= 60%: parecido, sugere
    private static final int MAX_SUGESTOES = 5;
    private static final int MAX_CANDIDATOS = 50;

    private final ServicoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final UnidadeCustoRepository unidadeCustoRepository;
    private final ReguaAderencia regua;
    private final IndiceServicos indice;

    public ServicoService(ServicoRepository repository,
                          CategoriaRepository categoriaRepository,
                          UnidadeMedidaRepository unidadeMedidaRepository,
                          UnidadeCustoRepository unidadeCustoRepository,
                          ReguaAderencia regua,
                          IndiceServicos indice) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.unidadeMedidaRepository = unidadeMedidaRepository;
        this.unidadeCustoRepository = unidadeCustoRepository;
        this.regua = regua;
        this.indice = indice;
    }

    public List<Servico> listarTodos() {
        return repository.findAll();
    }

    /** Catálogo paginado (por nome OU código). Tamanho máximo de página: 500. */
    @Transactional(readOnly = true)
    public Page<Servico> listarPagina(String q, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 500), Sort.by("nomeServico"));
        if (q == null || q.isBlank()) return repository.findAll(pageable);
        String termo = padronizar(q);
        return repository.findByNomeServicoContainingOrCodigoServicoContaining(termo, termo, pageable);
    }

    public List<Servico> buscarPorNome(String nome) {
        return repository.findByNomeServicoContaining(padronizar(nome));
    }

    // [ALTERADO, do Kaio] aceita só um dos dois filtros
    public List<Servico> buscarPorCategoriaNomeEServicoNome(String categoriaNome, String nomeServico) {
        boolean temCategoria = categoriaNome != null && !categoriaNome.isBlank();
        boolean temNome = nomeServico != null && !nomeServico.isBlank();
        if (temCategoria && temNome) {
            return repository.findByCategoria_CategoriaNomeAndNomeServicoContaining(padronizar(categoriaNome), padronizar(nomeServico));
        }
        if (temCategoria) {
            return repository.findByCategoria_CategoriaNomeContaining(padronizar(categoriaNome));
        }
        if (temNome) {
            return repository.findByNomeServicoContaining(padronizar(nomeServico));
        }
        return repository.findAll();
    }

    // [ALTERADO] antes buscava pelo NOME exato; agora o nome pode repetir em categorias diferentes,
    // então a tela de detalhes passa a identificar o serviço pelo CÓDIGO (a chave primária).
    public Optional<Servico> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return repository.findById(codigo.trim());
    }

    /** Candidatos da tela de consulta: nome contém o digitado (ou vice-versa), também comparando as formas
     *  normalizadas (assim "H EXTRA" acha "HORA EXTRA"). Usa o índice em memória e devolve no máximo 50. */
    public List<Servico> buscarCandidatos(String digitado) {
        String dPad = padronizar(digitado);
        if (dPad.isEmpty()) return new ArrayList<>();
        String dNorm = regua.perfil(digitado).normalizado();

        List<String> codigos = indice.entradas().stream()
                .filter(e -> e.nomePadrao().contains(dPad) || dPad.contains(e.nomePadrao())
                        || (!dNorm.isEmpty() && !e.perfil().normalizado().isEmpty()
                            && (e.perfil().normalizado().contains(dNorm) || dNorm.contains(e.perfil().normalizado()))))
                .sorted(Comparator.comparing(IndiceServicos.Entrada::nome))
                .limit(MAX_CANDIDATOS)
                .map(IndiceServicos.Entrada::codigo)
                .toList();
        List<Servico> achados = new ArrayList<>(repository.findAllById(codigos));
        achados.sort(Comparator.comparing(Servico::getNomeServico));
        return achados;
    }

    // ----------------------------------------------------------------- [NOVO, do Kaio] régua de aderência

    /** Serviços parecidos com o nome digitado, com a nota da régua (0 a 100%), usando o índice em memória.
     *  categoriaId = null compara com todas as categorias (usado na tela de consulta). */
    public List<SugestaoDTO> buscarSugestoes(Long categoriaId, String nome) {
        return indice.sugestoes(categoriaId, nome, MAX_SUGESTOES, LIMITE_SUGESTAO);
    }

    /** Sugestões da tela "não encontrado" e do cadastro (todas as categorias). */
    public List<SugestaoDTO> similares(String nome) {
        return indice.sugestoes(null, nome, MAX_SUGESTOES, LIMITE_SUGESTAO);
    }

    /** Cria um serviço PENDENTE com código gerado pelo sistema. Bloqueia (409) nome repetido na categoria
     *  e nome com aderência >= 90% a um existente da mesma categoria. */
    @Transactional
    public Servico criarNovo(ServicoDTO dto, String solicitante) {
        String nome = padronizar(dto.nomeServico());   // MAIÚSCULO, sem acento, espaços normalizados

        if (nome.length() > LIMITE_SAP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O nome passou de " + LIMITE_SAP + " caracteres (limite do SAP).");
        }

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> naoEncontrado("Categoria não encontrada."));

        if (repository.existsByCategoria_IdAndNomeServico(categoria.getId(), nome)) {
            throw conflito("Já existe um serviço com este nome na categoria " + categoria.getCategoriaNome() + ".");
        }

        List<SugestaoDTO> parecidos = buscarSugestoes(categoria.getId(), nome);
        if (!parecidos.isEmpty() && parecidos.getFirst().aderenciaPercentual() >= LIMITE_BLOQUEIO * 100) {
            SugestaoDTO igual = parecidos.getFirst();
            throw conflito("Este serviço já existe: " + igual.codigoServico() + " - " + igual.nomeServico());
        }

        Servico servico = new Servico();
        servico.setCodigoServico(gerarCodigo(categoria));   // só aqui, depois de validar tudo (não queima número)
        servico.setNomeServico(nome);
        servico.setDescricaoServico(dto.descricaoServico());
        servico.setCategoria(categoria);
        servico.setStatus(StatusServico.PENDENTE);   // aguarda aprovação
        servico.setSolicitadoPor(solicitante);
        if (dto.unidadeMedidaId() != null) {
            servico.setUnidadeMedida(unidadeMedidaRepository.findById(dto.unidadeMedidaId())
                    .orElseThrow(() -> naoEncontrado("Unidade de medida não encontrada.")));
        }
        if (dto.unidadeCustoId() != null) {
            servico.setUnidadeCusto(unidadeCustoRepository.findById(dto.unidadeCustoId())
                    .orElseThrow(() -> naoEncontrado("Unidade de custo não encontrada.")));
        }
        Servico salvo = repository.save(servico);
        indice.invalidarAposCommit();   // o índice em memória precisa enxergar o serviço novo
        return salvo;
    }

    /** Monta PREFIXO-000001 usando o contador da categoria (incremento atômico no banco). */
    private String gerarCodigo(Categoria categoria) {
        if (categoria.getPrefixo() == null || categoria.getPrefixo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A categoria " + categoria.getCategoriaNome() + " não tem prefixo configurado.");
        }
        // [PREFIXO] continua do ÚLTIMO número registrado na categoria (inclusive os códigos importados, ex.: FAC-4048071
        // -> próximo FAC-4048072). Se o contador da categoria estiver atrás, ele é elevado antes de incrementar.
        List<String> codigosExistentes = indice.entradas().stream().map(IndiceServicos.Entrada::codigo).toList();
        long maiorExistente = CodigoComPrefixo.maiorNumero(codigosExistentes, categoria.getPrefixo());
        if (maiorExistente > 0) {
            categoriaRepository.garantirUltimoNumero(categoria.getId(), maiorExistente);
        }
        // e pula números que já existem, para nunca gerar um código repetido
        for (int tentativa = 0; tentativa < 1000; tentativa++) {
            categoriaRepository.incrementarUltimoNumero(categoria.getId());
            long numero = categoriaRepository.buscarUltimoNumero(categoria.getId());
            String codigo = String.format("%s-%06d", categoria.getPrefixo(), numero);
            if (!repository.existsById(codigo)) return codigo;
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não consegui gerar um código livre para a categoria " + categoria.getCategoriaNome() + ".");
    }

    // ----------------------------------------------------------------- governança: aprovar / reprovar

    public List<Servico> listarPorStatus(StatusServico status) {
        return repository.findByStatusOrderByNomeServico(status);
    }

    /** [PERFORMANCE] Só código e nome (1 query leve). Usado nas listas suspensas da tela de vínculos. */
    public List<ServicoOpcaoView> listarOpcoesPorStatus(StatusServico status) {
        return repository.listarOpcoes(status);
    }

    @Transactional
    public Servico aprovar(String codigo) {
        Servico s = buscarPendente(codigo);
        s.setStatus(StatusServico.APROVADO);
        s.setMotivoRejeicao(null);
        indice.invalidarAposCommit();
        return s;   // o JPA grava sozinho no commit (dirty checking)
    }

    @Transactional
    public Servico reprovar(String codigo, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o motivo da reprovação.");
        }
        Servico s = buscarPendente(codigo);
        s.setStatus(StatusServico.REJEITADO);
        s.setMotivoRejeicao(motivo.trim());
        indice.invalidarAposCommit();
        return s;
    }

    private Servico buscarPendente(String codigo) {
        Servico s = repository.findById(codigo).orElseThrow(() -> naoEncontrado("Serviço não encontrado."));
        if (s.getStatus() != StatusServico.PENDENTE) {
            throw conflito("Este pedido já foi decidido (" + s.getStatus() + ").");
        }
        return s;
    }

    // ----------------------------------------------------------------- da Fernanda (sem mudanças)

    public String tratarTextoExtra(String digitado, Servico servico) {
        String d = padronizar(digitado);
        String nome = padronizar(servico.getNomeServico());
        if (d.equals(nome) || !d.contains(nome)) return "";
        return d.replace(nome, "").trim();
    }

    public String padronizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    private ResponseStatusException conflito(String mensagem) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
    }

    private ResponseStatusException naoEncontrado(String mensagem) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensagem);
    }
}
