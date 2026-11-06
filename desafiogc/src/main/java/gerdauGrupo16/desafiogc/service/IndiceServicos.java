package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: IndiceServicos.java
 * TIPO: componente do Spring (@Component).
 * FUNÇÃO: índice em memória de todos os serviços, com o perfil de texto já calculado, para a checagem de
 *   duplicado e a busca levarem milissegundos. É recarregado na próxima consulta depois que um serviço é
 *   criado, aprovado, reprovado ou importado. Se o banco for alterado por SQL, use Recarregar índice.
 *   Limite do MVP: funciona para uma instância da aplicação.
 * CLASSE INTERNA: Entrada (record) é um serviço no índice: código, nome, nome padronizado, categoria,
 *   status e perfil de texto.
 * MÉTODOS:
 * - IndiceServicos(repository, regua): recebe o repositório e a régua.
 * - entradas(): lista atual do índice (carrega se estiver vazio).
 * - carregar(): lê o banco e monta o índice; não guarda a lista se alguém invalidou durante a carga.
 * - invalidar(): descarta o índice agora.
 * - invalidarAposCommit(): descarta o índice só depois do commit da transação, para ninguém recarregar
 *   antes de o dado existir.
 * - afterCommit() (método do callback interno): chama invalidar() quando o commit termina.
 * - sugestoes(categoriaId, nome, max, minimo): serviços parecidos com o nome, com nota igual ou maior que o
 *   mínimo, sem os REJEITADOS, da melhor para a pior.
 * - padrao(texto): maiúsculas, sem acento, espaços simples.
 */

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
        if (versao.get() == v) cache = lista;
        return lista;
    }

    public void invalidar() {
        versao.incrementAndGet();
        cache = null;
    }

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
