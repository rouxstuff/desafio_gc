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
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class ServicoService {

    private static final double LIMITE_BLOQUEIO = 0.90;   // >= 90%: duplicado, bloqueia (409)
    private static final double LIMITE_SUGESTAO = 0.60;   // >= 60%: parecido, sugere
    private static final int MAX_SUGESTOES = 5;

    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final UnidadeCustoRepository unidadeCustoRepository;
    private final ReguaAderencia calculadora;

    public List<Servico> listarTodosServicos() {
        return servicoRepository.findAll(Sort.by("nomeServico"));
    }

    public List<Servico> buscarPorNome(String nome) {
        return servicoRepository.findByNomeServicoContaining(padronizar(nome));
    }
    public List<Servico> buscarPorCategoriaNomeEServicoNome(String categoriaNome, String nomeServico) {
        if(categoriaNome != null && nomeServico != null) {
            return servicoRepository.findByCategoria_CategoriaNomeAndNomeServicoContaining(padronizar(categoriaNome), padronizar(nomeServico));
        }

        if(categoriaNome != null) {
            return servicoRepository.findByCategoria_CategoriaNomeContaining(padronizar(categoriaNome));
        }

        if(nomeServico != null) {
            return servicoRepository.findByNomeServicoContaining(padronizar(nomeServico));
        }

        return servicoRepository.findAll();
    }

    //Regua de aderencia e Sugestão
    @Transactional(readOnly = true)
    public List<SugestaoDTO> buscarSugestoes(Long categoriaId, String nome) {
        String nomePadrao = padronizar(nome);
        return servicoRepository
                .findByCategoria_IdAndStatusIn(categoriaId, List.of(StatusServico.APROVADO, StatusServico.PENDENTE))
                .stream()
                .map(s -> SugestaoDTO.de(s.getCodigoServico(), s.getNomeServico(),
                        calculadora.calcular(nomePadrao, s.getNomeServico())))
                .filter(s -> s.aderenciaPercentual() >= LIMITE_SUGESTAO * 100)
                .sorted(Comparator.comparingDouble(SugestaoDTO::aderenciaPercentual).reversed())
                .limit(MAX_SUGESTOES)
                .toList();
    }
    //Cria um novo serviço e caso exista um serviço parecido, dá sugestões antes de criar
    @Transactional
    public Servico criarNovo(ServicoDTO dto) {
        String codigo = dto.codigoServico().trim();
        String nome = padronizar(dto.nomeServico());   // MAIÚSCULO, sem acento, espaços normalizados

        if (servicoRepository.existsById(codigo)) {
            throw conflito("O código " + codigo + " já existe.");
        }

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> naoEncontrado("Categoria não encontrada."));

        if (servicoRepository.existsByCategoria_IdAndNomeServico(categoria.getId(), nome)) {
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
        return servicoRepository.save(servico);
    }

    private ResponseStatusException conflito(String mensagem) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
    }

    private ResponseStatusException naoEncontrado(String mensagem) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensagem);
    }

    private String padronizar(String texto) {
        String semAcento = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
