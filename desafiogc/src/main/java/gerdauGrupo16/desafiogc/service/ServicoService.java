package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.controller.dto.ServicoDTO;
import gerdauGrupo16.desafiogc.controller.dto.SugestaoDTO;
import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.repository.CategoriaRepository;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import gerdauGrupo16.desafiogc.repository.UnidadeCustoRepository;
import gerdauGrupo16.desafiogc.repository.UnidadeMedidaRepository;
import gerdauGrupo16.desafiogc.service.reguaAderencia.ReguaAderencia;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    private final ServicoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final UnidadeCustoRepository unidadeCustoRepository;
    private final ReguaAderencia regua;

    public ServicoService(ServicoRepository repository,
                          CategoriaRepository categoriaRepository,
                          UnidadeMedidaRepository unidadeMedidaRepository,
                          UnidadeCustoRepository unidadeCustoRepository,
                          ReguaAderencia regua) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.unidadeMedidaRepository = unidadeMedidaRepository;
        this.unidadeCustoRepository = unidadeCustoRepository;
        this.regua = regua;
    }

    public List<Servico> listarTodos() {
        return repository.findAll();
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

    public List<Servico> buscarCandidatos(String digitado) {
        String d = padronizar(digitado);
        List<Servico> candidatos = new ArrayList<>();
        if (d.isEmpty()) return candidatos;

        // depois esse metodo sera substituido pela busca por similaridade
        for (Servico s : repository.findAll()) {
            String nome = padronizar(s.getNomeServico());
            if (nome.contains(d) || d.contains(nome)) {
                candidatos.add(s);
            }
        }
        return candidatos;
    }

    // ----------------------------------------------------------------- [NOVO, do Kaio] régua de aderência

    /** Serviços parecidos com o nome digitado, com a nota da régua (0 a 100%).
     *  categoriaId = null compara com todas as categorias (usado na tela de consulta). */
    @Transactional(readOnly = true)
    public List<SugestaoDTO> buscarSugestoes(Long categoriaId, String nome) {
        String nomePadrao = padronizar(nome);
        List<StatusServico> validos = List.of(StatusServico.APROVADO, StatusServico.PENDENTE);
        List<Servico> base = categoriaId == null
                ? repository.findByStatusIn(validos)
                : repository.findByCategoria_IdAndStatusIn(categoriaId, validos);
        return base.stream()
                .map(s -> SugestaoDTO.de(s.getCodigoServico(), s.getNomeServico(),
                        regua.calcular(nomePadrao, s.getNomeServico())))
                .filter(s -> s.aderenciaPercentual() >= LIMITE_SUGESTAO * 100)
                .sorted(Comparator.comparingDouble(SugestaoDTO::aderenciaPercentual).reversed())
                .limit(MAX_SUGESTOES)
                .toList();
    }

    /** [ALTERADO] Antes: só "mesmo começo de palavra" (da Fernanda). Agora: primeiro a régua do Kaio
     *  (com porcentagem) e, para completar, o critério antigo, que ainda pega erro de digitação
     *  (ELETRICSTA acha ELETRICISTA), que a régua simples não pega. */
    @Transactional(readOnly = true)
    public List<SugestaoDTO> similares(String nome) {
        List<SugestaoDTO> resultado = new ArrayList<>(buscarSugestoes(null, nome));
        List<Servico> todos = repository.findAll();

        for (String palavra : padronizar(nome).split(" ")) {
            if (palavra.length() <= 3) continue;
            String comeco = palavra.substring(0, 4);
            for (Servico s : todos) {
                boolean jaTem = resultado.stream().anyMatch(r -> r.codigoServico().equals(s.getCodigoServico()));
                if (!jaTem && padronizar(s.getNomeServico()).contains(comeco)) {
                    resultado.add(SugestaoDTO.de(s.getCodigoServico(), s.getNomeServico(),
                            regua.calcular(nome, s.getNomeServico())));
                }
            }
        }
        return resultado.subList(0, Math.min(MAX_SUGESTOES, resultado.size()));
    }

    /** Cria um serviço PENDENTE. Bloqueia (409) código repetido, nome repetido na categoria
     *  e nome com aderência >= 90% a um existente da mesma categoria. */
    @Transactional
    public Servico criarNovo(ServicoDTO dto) {
        String codigo = dto.codigoServico().trim();
        String nome = padronizar(dto.nomeServico());   // MAIÚSCULO, sem acento, espaços normalizados

        if (nome.length() > LIMITE_SAP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O nome passou de " + LIMITE_SAP + " caracteres (limite do SAP).");
        }
        // Precisa checar aqui: o save() faz "merge" e SOBRESCREVERIA o serviço existente.
        if (repository.existsById(codigo)) {
            throw conflito("O código " + codigo + " já existe.");
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
        servico.setCodigoServico(codigo);
        servico.setNomeServico(nome);
        servico.setDescricaoServico(dto.descricaoServico());
        servico.setCategoria(categoria);
        servico.setStatus(StatusServico.PENDENTE);   // aguarda aprovação
        if (dto.unidadeMedidaId() != null) {
            servico.setUnidadeMedida(unidadeMedidaRepository.findById(dto.unidadeMedidaId())
                    .orElseThrow(() -> naoEncontrado("Unidade de medida não encontrada.")));
        }
        if (dto.unidadeCustoId() != null) {
            servico.setUnidadeCusto(unidadeCustoRepository.findById(dto.unidadeCustoId())
                    .orElseThrow(() -> naoEncontrado("Unidade de custo não encontrada.")));
        }
        return repository.save(servico);
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
