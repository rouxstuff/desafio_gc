package gerdauGrupo16.desafiogc.service;

import gerdauGrupo16.desafiogc.controller.dto.SugestaoDTO;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import gerdauGrupo16.desafiogc.service.reguaAderencia.NormalizadorTexto;
import gerdauGrupo16.desafiogc.service.reguaAderencia.PerfilTexto;
import gerdauGrupo16.desafiogc.service.reguaAderencia.ReguaAderencia;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Índice em memória de todos os serviços, com o perfil de texto (normalização, palavras, trigramas) JÁ calculado.
 *
 * Por quê: sem ele, cada checagem de duplicata lia e normalizava milhares de linhas. Com ele, a checagem
 * é só comparar o texto digitado com perfis prontos (cerca de 8 ms para ~8 mil serviços, medido com a base real).
 *
 * O índice é recarregado sozinho na próxima consulta depois que um serviço é criado, aprovado ou reprovado,
 * ou quando a importação termina. Se o banco for alterado por fora (SQL direto), use "Recarregar índice" no painel do admin.
 * Limite conhecido do MVP: funciona para UMA instância da aplicação.
 */
@Component
public class IndiceServicos {

    public record Entrada(String codigo, String nome, String nomePadrao, Long categoriaId,
                          StatusServico status, PerfilTexto perfil) { }

    private final ServicoRepository repository;
    private final ReguaAderencia regua;
    private final AtomicLong versao = new AtomicLong();
    private volatile List<Entrada> cache;

    public IndiceServicos(ServicoRepository repository, ReguaAderencia regua) {
        this.repository = repository;
        this.regua = regua;
    }

    public List<Entrada> entradas() {
        List<Entrada> atual = cache;
        return atual != null ? atual : carregar();
    }

    private synchronized List<Entrada> carregar() {
        if (cache != null) return cache;
        long v = versao.get();
        List<Entrada> lista = repository.listarParaIndice().stream()
                .map(s -> new Entrada(s.getCodigoServico(), s.getNomeServico(), padrao(s.getNomeServico()),
                        s.getCategoriaId(), s.getStatus(), regua.perfil(s.getNomeServico())))
                .toList();
        if (versao.get() == v) cache = lista;   // se alguém invalidou durante a carga, não guarda a lista velha
        return lista;
    }

    public void invalidar() {
        versao.incrementAndGet();
        cache = null;
    }

    /** Invalida só DEPOIS do commit (senão outra consulta poderia recarregar antes de o dado existir). */
    public void invalidarAposCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    invalidar();
                }
            });
        } else {
            invalidar();
        }
    }

    /** Serviços parecidos (nota >= minimo, de 0 a 1), ignorando os REJEITADOS. categoriaId = null compara com todas. */
    public List<SugestaoDTO> sugestoes(Long categoriaId, String nome, int max, double minimo) {
        PerfilTexto alvo = regua.perfil(nome);
        if (alvo.tokens().isEmpty()) return List.of();
        return entradas().stream()
                .filter(e -> e.status() != StatusServico.REJEITADO)
                .filter(e -> categoriaId == null || categoriaId.equals(e.categoriaId()))
                .map(e -> SugestaoDTO.de(e.codigo(), e.nome(), regua.calcular(alvo, e.perfil())))
                .filter(s -> s.aderenciaPercentual() >= minimo * 100)
                .sorted(Comparator.comparingDouble(SugestaoDTO::aderenciaPercentual).reversed()
                        .thenComparing(SugestaoDTO::nomeServico))
                .limit(max)
                .toList();
    }

    private static String padrao(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
