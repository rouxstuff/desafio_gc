package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: ServicoService.java
 * TIPO: classe de serviço (@Service). É o coração das regras de negócio de serviço.
 * FUNÇÃO: consulta, sugestão de parecidos, criação com bloqueio de duplicados, geração de código, alteração
 *   de características e aprovação ou reprovação. Limites: nome até 40 caracteres (LIMITE_SAP); a nota da
 *   régua de 90% ou mais bloqueia (LIMITE_BLOQUEIO) e de 60% ou mais sugere (LIMITE_SUGESTAO); no máximo 5
 *   sugestões e 50 candidatos de busca.
 * CLASSE INTERNA: BuscaMultipla (record) é o resultado de pesquisar vários termos de uma vez: termos,
 *   candidatos, de qual termo cada serviço veio e termos sem resultado.
 * MÉTODOS:
 * - listarTodos(): todos os serviços.
 * - visivelPara(servico, admin): regra de visibilidade; REJEITADO só o administrador vê.
 * - listarPagina(q, pagina, tamanho, incluirRejeitados): catálogo paginado (máximo 500 por página), por
 *   nome ou código, ordenado por nome.
 * - buscarPorNome(nome): serviços cujo nome contém o texto.
 * - buscarPorCategoriaNomeEServicoNome(categoriaNome, nomeServico, incluirRejeitados): filtra por categoria
 *   e/ou nome; aceita só um dos dois.
 * - buscarPorCodigo(codigo): acha pelo código, a chave do serviço.
 * - buscarCandidatos(digitado, incluirRejeitados): candidatos da tela de consulta usando o índice em
 *   memória (nome contém o digitado ou vice-versa, também na forma normalizada, assim H EXTRA acha HORA
 *   EXTRA). Até 50.
 * - buscarMultiplo(digitado, incluirRejeitados): pesquisa vários termos separados por ponto e vírgula e
 *   junta os resultados sem repetir.
 * - buscarSugestoes(categoriaId, nome): serviços parecidos com a nota da régua; categoriaId nulo compara
 *   com todas.
 * - similares(nome): sugestões de todas as categorias (tela não encontrado e cadastro).
 * - criarNovo(dto, solicitante): cria o serviço PENDENTE. Recusa nome acima de 40 (400), categoria
 *   inexistente (404), mesmo nome na categoria (409) e nota de 90% ou mais na mesma categoria (409). O
 *   código só é gerado depois das validações.
 * - gerarCodigo(categoria): monta PREFIXO-NÚMERO continuando do maior número da categoria, com o contador
 *   do banco, e pula números que já existem.
 * - listarPorStatus(status): serviços de um status, por nome (fila de aprovação).
 * - buscarOpcoes(status, texto, max): busca leve por nome ou código para a tela de vínculos; ignora os
 *   curingas % e _.
 * - listarOpcoesPorStatus(status): código e nome de todos os serviços de um status.
 * - atualizarCaracteristicas(codigo, unidadeMedidaId, unidadeCustoId, descricao): altera unidade de medida,
 *   unidade de custo e descrição (até 100). Unidade nula remove a unidade.
 * - aprovar(codigo): PENDENTE vira APROVADO.
 * - reprovar(codigo, motivo): PENDENTE vira REJEITADO; o motivo é obrigatório.
 * - buscarPendente(codigo): método auxiliar; acha o serviço e confirma que ainda está PENDENTE (senão 409).
 * - tratarTextoExtra(digitado, servico): devolve o que foi digitado além do nome do serviço (ex.: hora
 *   extra), para avisar o usuário.
 * - padronizar(texto): maiúsculas, sem acento e com espaços simples.
 * - conflito(mensagem) e naoEncontrado(mensagem): criam os erros 409 e 404.
 */

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ServicoService {
    public static final int LIMITE_SAP = 40;

    public static final double LIMITE_BLOQUEIO = 0.90;
    public static final double LIMITE_SUGESTAO = 0.60;
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

    public static boolean visivelPara(Servico servico, boolean admin) {
        return admin || servico.getStatus() != StatusServico.REJEITADO;
    }

    @Transactional(readOnly = true)
    public Page<Servico> listarPagina(String q, int pagina, int tamanho, boolean incluirRejeitados) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 500), Sort.by("nomeServico"));
        if (q == null || q.isBlank()) {
            return incluirRejeitados ? repository.findAll(pageable) : repository.findByStatusNot(StatusServico.REJEITADO, pageable);
        }
        String termo = padronizar(q);
        return incluirRejeitados
                ? repository.findByNomeServicoContainingOrCodigoServicoContaining(termo, termo, pageable)
                : repository.buscarPaginaSemStatus(StatusServico.REJEITADO, termo, pageable);
    }

    public List<Servico> buscarPorNome(String nome) {
        return repository.findByNomeServicoContaining(padronizar(nome));
    }

    public List<Servico> buscarPorCategoriaNomeEServicoNome(String categoriaNome, String nomeServico, boolean incluirRejeitados) {
        boolean temCategoria = categoriaNome != null && !categoriaNome.isBlank();
        boolean temNome = nomeServico != null && !nomeServico.isBlank();
        StatusServico fora = StatusServico.REJEITADO;
        if (temCategoria && temNome) {
            return incluirRejeitados
                    ? repository.findByCategoria_CategoriaNomeAndNomeServicoContaining(padronizar(categoriaNome), padronizar(nomeServico))
                    : repository.findByCategoria_CategoriaNomeAndNomeServicoContainingAndStatusNot(padronizar(categoriaNome), padronizar(nomeServico), fora);
        }
        if (temCategoria) {
            return incluirRejeitados
                    ? repository.findByCategoria_CategoriaNomeContaining(padronizar(categoriaNome))
                    : repository.findByCategoria_CategoriaNomeContainingAndStatusNot(padronizar(categoriaNome), fora);
        }
        if (temNome) {
            return incluirRejeitados
                    ? repository.findByNomeServicoContaining(padronizar(nomeServico))
                    : repository.findByNomeServicoContainingAndStatusNot(padronizar(nomeServico), fora);
        }
        return incluirRejeitados ? repository.findAll() : repository.findByStatusNot(fora);
    }

    public Optional<Servico> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return repository.findById(codigo.trim());
    }

    public List<Servico> buscarCandidatos(String digitado, boolean incluirRejeitados) {
        String dPad = padronizar(digitado);
        if (dPad.isEmpty()) return new ArrayList<>();
        String dNorm = regua.perfil(digitado).normalizado();

        List<String> codigos = indice.entradas().stream()
                .filter(e -> incluirRejeitados || e.status() != StatusServico.REJEITADO)
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

    public record BuscaMultipla(List<String> termos, List<Servico> candidatos,
                                Map<String, List<String>> termosPorCodigo, List<String> semResultado) { }

    public BuscaMultipla buscarMultiplo(String digitado, boolean incluirRejeitados) {
        List<String> termos = TermosBusca.separar(digitado);
        Map<String, Servico> unicos = new LinkedHashMap<>();
        Map<String, List<String>> termosPorCodigo = new LinkedHashMap<>();
        List<String> semResultado = new ArrayList<>();
        for (String termo : termos) {
            List<Servico> achados = buscarCandidatos(termo, incluirRejeitados);
            if (achados.isEmpty()) semResultado.add(termo);
            for (Servico s : achados) {
                unicos.putIfAbsent(s.getCodigoServico(), s);
                termosPorCodigo.computeIfAbsent(s.getCodigoServico(), k -> new ArrayList<>()).add(termo);
            }
        }
        List<Servico> todos = new ArrayList<>(unicos.values());
        todos.sort(Comparator.comparing(Servico::getNomeServico));
        return new BuscaMultipla(termos, todos, termosPorCodigo, semResultado);
    }

    public List<SugestaoDTO> buscarSugestoes(Long categoriaId, String nome) {
        return indice.sugestoes(categoriaId, nome, MAX_SUGESTOES, LIMITE_SUGESTAO);
    }

    public List<SugestaoDTO> similares(String nome) {
        return indice.sugestoes(null, nome, MAX_SUGESTOES, LIMITE_SUGESTAO);
    }

    @Transactional
    public Servico criarNovo(ServicoDTO dto, String solicitante) {
        String nome = padronizar(dto.nomeServico());

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
        servico.setCodigoServico(gerarCodigo(categoria));
        servico.setNomeServico(nome);
        servico.setDescricaoServico(dto.descricaoServico());
        servico.setCategoria(categoria);
        servico.setStatus(StatusServico.PENDENTE);
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
        indice.invalidarAposCommit();
        return salvo;
    }

    private String gerarCodigo(Categoria categoria) {
        if (categoria.getPrefixo() == null || categoria.getPrefixo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A categoria " + categoria.getCategoriaNome() + " não tem prefixo configurado.");
        }

        List<String> codigosExistentes = indice.entradas().stream().map(IndiceServicos.Entrada::codigo).toList();
        long maiorExistente = CodigoComPrefixo.maiorNumero(codigosExistentes, categoria.getPrefixo());
        if (maiorExistente > 0) {
            categoriaRepository.garantirUltimoNumero(categoria.getId(), maiorExistente);
        }

        for (int tentativa = 0; tentativa < 1000; tentativa++) {
            categoriaRepository.incrementarUltimoNumero(categoria.getId());
            long numero = categoriaRepository.buscarUltimoNumero(categoria.getId());
            String codigo = String.format("%s-%06d", categoria.getPrefixo(), numero);
            if (!repository.existsById(codigo)) return codigo;
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não consegui gerar um código livre para a categoria " + categoria.getCategoriaNome() + ".");
    }

    public List<Servico> listarPorStatus(StatusServico status) {
        return repository.findByStatusOrderByNomeServico(status);
    }

    @Transactional(readOnly = true)
    public List<ServicoOpcaoView> buscarOpcoes(StatusServico status, String texto, int max) {

        String termo = padronizar(texto).replace("%", "").replace("_", "").trim();
        if (termo.isEmpty()) return List.of();
        return repository.buscarOpcoes(status, termo, PageRequest.of(0, Math.max(max, 1)));
    }

    public List<ServicoOpcaoView> listarOpcoesPorStatus(StatusServico status) {
        return repository.listarOpcoes(status);
    }

    public static final int LIMITE_DESCRICAO = 100;

    @Transactional
    public Servico atualizarCaracteristicas(String codigo, Long unidadeMedidaId, Long unidadeCustoId, String descricao) {
        Servico s = repository.findById(codigo).orElseThrow(() -> naoEncontrado("Serviço não encontrado."));
        String desc = descricao == null ? null : descricao.strip();
        if (desc != null && desc.length() > LIMITE_DESCRICAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A descrição deve ter no máximo " + LIMITE_DESCRICAO + " caracteres.");
        }
        s.setUnidadeMedida(unidadeMedidaId == null ? null : unidadeMedidaRepository.findById(unidadeMedidaId)
                .orElseThrow(() -> naoEncontrado("Unidade de medida não encontrada.")));
        s.setUnidadeCusto(unidadeCustoId == null ? null : unidadeCustoRepository.findById(unidadeCustoId)
                .orElseThrow(() -> naoEncontrado("Unidade de custo não encontrada.")));
        s.setDescricaoServico(desc == null || desc.isEmpty() ? null : desc);
        return s;
    }

    @Transactional
    public Servico aprovar(String codigo) {
        Servico s = buscarPendente(codigo);
        s.setStatus(StatusServico.APROVADO);
        s.setMotivoRejeicao(null);
        indice.invalidarAposCommit();
        return s;
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
