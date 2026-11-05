package gerdauGrupo16.desafiogc.service.importacao;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.FornecedorServico;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.repository.CategoriaRepository;
import gerdauGrupo16.desafiogc.repository.FornecedorRepository;
import gerdauGrupo16.desafiogc.repository.FornecedorServicoRepository;
import gerdauGrupo16.desafiogc.repository.ParVinculoView;
import gerdauGrupo16.desafiogc.repository.ServicoIndiceView;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import gerdauGrupo16.desafiogc.repository.UnidadeMedidaRepository;
import gerdauGrupo16.desafiogc.service.IndiceServicos;
import gerdauGrupo16.desafiogc.service.ServicoService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Importa o catálogo SANEADO (e os vínculos) a partir de CSV.
 *
 * Regras de segurança:
 *  - TUDO OU NADA: se qualquer linha tiver erro, nada é gravado e o relatório lista os erros (com o número da linha).
 *  - "Simular" valida tudo e mostra o que seria feito, sem gravar nada.
 *  - Pode rodar de novo com o mesmo arquivo: códigos que já existem são pulados (não duplica, não sobrescreve).
 *
 * Colunas de SERVIÇOS (apelidos aceitos entre parênteses; aceita também os nomes da planilha original):
 *   codigo_servico (codigo_do_servico), nome_servico (descricao_do_item), categoria (grupo_de_servico),
 *   unidade_medida (opcional), descricao_servico (opcional), status (opcional, padrão APROVADO),
 *   prefixo_categoria (opcional: só é usado se a categoria ainda NÃO existe, para criá-la)
 * Colunas de VÍNCULOS: codigo_fornecedor (codigo_do_fornecedor), codigo_servico (codigo_do_servico)
 */
@Service
public class ImportacaoService {

    public static final int MAX_MENSAGENS = 200;
    private static final int LOTE = 500;

    private static final String[] CODIGO_SERVICO = {"codigo_servico", "codigo_do_servico", "codigo"};
    private static final String[] NOME = {"nome_servico", "descricao_do_item", "nome"};
    private static final String[] CATEGORIA = {"categoria", "grupo_de_servico", "grupo"};
    private static final String[] PREFIXO = {"prefixo_categoria", "prefixo"};
    private static final String[] UNIDADE = {"unidade_medida", "unidade", "sigla_unidade_medida"};
    private static final String[] DESCRICAO = {"descricao_servico", "descricao"};
    private static final String[] STATUS = {"status"};
    private static final String[] CODIGO_FORNECEDOR = {"codigo_fornecedor", "codigo_do_fornecedor", "fornecedor"};

    public record Relatorio(String tipo, boolean simulacao, boolean aplicado, int linhasLidas, int criados,
                            int jaExistiam, int repetidas, int categoriasCriadas, int fornecedoresCriados,
                            List<String> erros, int totalErros, List<String> avisos) { }

    private record Par(String fornecedor, String servico) { }

    private static final class Mensagens {
        final List<String> lista = new ArrayList<>();
        int total = 0;
        void add(String m) { total++; if (lista.size() < MAX_MENSAGENS) lista.add(m); }
    }

    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final FornecedorServicoRepository fornecedorServicoRepository;
    private final ServicoService servicoService;
    private final IndiceServicos indice;
    private final EntityManager em;

    public ImportacaoService(ServicoRepository servicoRepository, CategoriaRepository categoriaRepository,
                             UnidadeMedidaRepository unidadeMedidaRepository, FornecedorRepository fornecedorRepository,
                             FornecedorServicoRepository fornecedorServicoRepository, ServicoService servicoService,
                             IndiceServicos indice, EntityManager em) {
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
        this.unidadeMedidaRepository = unidadeMedidaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.fornecedorServicoRepository = fornecedorServicoRepository;
        this.servicoService = servicoService;
        this.indice = indice;
        this.em = em;
    }

    // ------------------------------------------------------------------------------------------ SERVIÇOS

    @Transactional
    public Relatorio importarServicos(byte[] bytes, boolean simular) {
        CsvLeitor.Resultado csv = CsvLeitor.ler(bytes);
        Mensagens erros = new Mensagens();
        Mensagens avisos = new Mensagens();

        if (!csv.temColuna(CODIGO_SERVICO)) erros.add("Não achei a coluna do código (use o cabeçalho: codigo_servico).");
        if (!csv.temColuna(NOME)) erros.add("Não achei a coluna do nome (use o cabeçalho: nome_servico).");
        if (!csv.temColuna(CATEGORIA)) erros.add("Não achei a coluna da categoria (use o cabeçalho: categoria).");
        if (erros.total > 0) {
            return new Relatorio("serviços", simular, false, csv.linhas().size(), 0, 0, 0, 0, 0, erros.lista, erros.total, avisos.lista);
        }

        Map<String, Categoria> categorias = new HashMap<>();
        Map<Long, String> nomeCategoriaPorId = new HashMap<>();
        Set<String> prefixosEmUso = new HashSet<>();
        for (Categoria c : categoriaRepository.findAll()) {
            String chave = servicoService.padronizar(c.getCategoriaNome());
            categorias.put(chave, c);
            nomeCategoriaPorId.put(c.getId(), chave);
            if (c.getPrefixo() != null) prefixosEmUso.add(c.getPrefixo());
        }
        Map<String, UnidadeMedida> unidades = new HashMap<>();
        for (UnidadeMedida u : unidadeMedidaRepository.findAll()) {
            if (u.getCodigo() != null) unidades.put(u.getCodigo().toUpperCase(), u);
        }
        Set<String> codigosExistentes = new HashSet<>(servicoRepository.listarCodigos());
        Map<String, String> nomesExistentes = new HashMap<>();          // "CATEGORIA|NOME" -> código
        for (ServicoIndiceView v : servicoRepository.listarParaIndice()) {
            String cat = nomeCategoriaPorId.get(v.getCategoriaId());
            if (cat != null) nomesExistentes.put(cat + "|" + servicoService.padronizar(v.getNomeServico()), v.getCodigoServico());
        }

        Map<String, Servico> novos = new LinkedHashMap<>();
        Map<String, String> assinaturas = new HashMap<>();              // código -> "CATEGORIA|NOME|UNIDADE"
        Map<String, String> nomesNovos = new HashMap<>();               // "CATEGORIA|NOME" -> código
        List<Categoria> categoriasNovas = new ArrayList<>();
        int jaExistiam = 0;
        int repetidas = 0;

        for (CsvLeitor.Linha l : csv.linhas()) {
            String ref = "Linha " + l.numero() + ": ";
            String codigo = limparCodigo(l.get(CODIGO_SERVICO));
            String nome = servicoService.padronizar(l.get(NOME));
            String catTxt = servicoService.padronizar(l.get(CATEGORIA));
            String unTxt = l.get(UNIDADE).toUpperCase();
            String descricao = l.get(DESCRICAO);
            String statusTxt = l.get(STATUS).toUpperCase();

            if (codigo.isEmpty()) { erros.add(ref + "código do serviço vazio."); continue; }
            if (codigo.length() > 20) { erros.add(ref + "código '" + codigo + "' passa de 20 caracteres."); continue; }
            if (nome.isEmpty()) { erros.add(ref + "nome vazio (código " + codigo + ")."); continue; }
            if (nome.length() > ServicoService.LIMITE_SAP) {
                erros.add(ref + "nome do código " + codigo + " passa de " + ServicoService.LIMITE_SAP + " caracteres (limite do SAP).");
                continue;
            }
            if (catTxt.isEmpty()) { erros.add(ref + "categoria vazia (código " + codigo + ")."); continue; }
            if (descricao.length() > 100) { erros.add(ref + "descrição do código " + codigo + " passa de 100 caracteres."); continue; }

            StatusServico status = StatusServico.APROVADO;
            if (!statusTxt.isEmpty()) {
                try {
                    status = StatusServico.valueOf(statusTxt);
                } catch (IllegalArgumentException e) {
                    erros.add(ref + "status '" + statusTxt + "' inválido (use APROVADO, PENDENTE ou REJEITADO).");
                    continue;
                }
            }
            UnidadeMedida unidade = null;
            if (!unTxt.isEmpty()) {
                unidade = unidades.get(unTxt);
                if (unidade == null) { erros.add(ref + "unidade de medida '" + unTxt + "' não existe."); continue; }
            }

            // [PREFIXO] o código existente é mantido e ganha o prefixo da categoria (CIV-4057137). Já com prefixo: não muda.
            Categoria categoriaConhecida = categorias.get(catTxt);
            String prefixoDaCategoria = categoriaConhecida != null
                    ? categoriaConhecida.getPrefixo() : servicoService.padronizar(l.get(PREFIXO));
            codigo = CodigoComPrefixo.aplicar(codigo, prefixoDaCategoria);
            if (codigo.length() > 20) { erros.add(ref + "código '" + codigo + "' (com o prefixo) passa de 20 caracteres."); continue; }

            if (codigosExistentes.contains(codigo)) { jaExistiam++; continue; }

            Categoria categoria = categorias.get(catTxt);
            if (categoria == null) {
                String prefixo = servicoService.padronizar(l.get(PREFIXO));
                if (prefixo.isEmpty()) {
                    erros.add(ref + "a categoria '" + catTxt + "' não existe. Para criá-la, preencha a coluna prefixo_categoria.");
                    continue;
                }
                if (!prefixo.matches("[A-Z]{2,10}")) {
                    erros.add(ref + "prefixo '" + prefixo + "' inválido (use de 2 a 10 letras, sem acento).");
                    continue;
                }
                if (prefixosEmUso.contains(prefixo)) {
                    erros.add(ref + "o prefixo '" + prefixo + "' já pertence a outra categoria.");
                    continue;
                }
                categoria = new Categoria(catTxt, prefixo);
                categorias.put(catTxt, categoria);
                categoriasNovas.add(categoria);
                prefixosEmUso.add(prefixo);
            }

            String chaveNome = catTxt + "|" + nome;
            String assinatura = chaveNome + "|" + unTxt;
            if (assinaturas.containsKey(codigo)) {
                if (assinaturas.get(codigo).equals(assinatura)) repetidas++;
                else erros.add(ref + "o código " + codigo + " aparece de novo com dados diferentes (nome, categoria ou unidade).");
                continue;
            }
            String outro = nomesExistentes.get(chaveNome);
            if (outro == null) outro = nomesNovos.get(chaveNome);
            if (outro != null) {
                erros.add(ref + "o nome '" + nome + "' já é usado pelo código " + outro + " na categoria " + catTxt
                        + " (duplicado: resolva no saneamento).");
                continue;
            }

            Servico s = new Servico();
            s.setCodigoServico(codigo);
            s.setNomeServico(nome);
            s.setDescricaoServico(descricao.isEmpty() ? null : descricao);
            s.setCategoria(categoria);
            s.setUnidadeMedida(unidade);
            s.setStatus(status);
            s.setSolicitadoPor("importacao");
            novos.put(codigo, s);
            assinaturas.put(codigo, assinatura);
            nomesNovos.put(chaveNome, codigo);
        }

        if (repetidas > 0) avisos.add(repetidas + " linha(s) repetida(s) (mesmo código com os mesmos dados) foram ignoradas.");
        if (jaExistiam > 0) avisos.add(jaExistiam + " linha(s) com código que já existe no banco foram puladas (nada foi alterado).");

        boolean gravar = erros.total == 0 && !simular;
        if (gravar) {
            for (Categoria c : categoriasNovas) em.persist(c);
            int n = 0;
            for (Servico s : novos.values()) {
                em.persist(s);
                if (++n % LOTE == 0) { em.flush(); em.clear(); }
            }
            em.flush();
            sincronizarContadores(novos.values());
            indice.invalidarAposCommit();
        }
        return new Relatorio("serviços", simular, gravar, csv.linhas().size(), novos.size(), jaExistiam, repetidas,
                categoriasNovas.size(), 0, erros.lista, erros.total, avisos.lista);
    }

    /** Se vierem códigos no formato PREFIXO-NÚMERO (inclusive os do SAP, ex.: CIV-4057137), o contador da categoria não pode
     *  ficar atrás deles: o próximo código gerado continua do último número da categoria. */
    private void sincronizarContadores(Iterable<Servico> importados) {
        Map<Long, Long> maximos = new HashMap<>();
        for (Servico s : importados) {
            Categoria c = s.getCategoria();
            if (c == null || c.getPrefixo() == null) continue;
            String pre = c.getPrefixo() + "-";
            String cod = s.getCodigoServico();
            if (!cod.startsWith(pre) || cod.length() == pre.length() || cod.length() - pre.length() > 18) continue;
            String resto = cod.substring(pre.length());
            if (resto.chars().allMatch(Character::isDigit)) maximos.merge(c.getId(), Long.parseLong(resto), Long::max);
        }
        for (Map.Entry<Long, Long> e : maximos.entrySet()) {
            categoriaRepository.garantirUltimoNumero(e.getKey(), e.getValue());
        }
    }

    // ------------------------------------------------------------------------------------------ VÍNCULOS

    @Transactional
    public Relatorio importarVinculos(byte[] bytes, boolean simular) {
        CsvLeitor.Resultado csv = CsvLeitor.ler(bytes);
        Mensagens erros = new Mensagens();
        Mensagens avisos = new Mensagens();

        if (!csv.temColuna(CODIGO_FORNECEDOR)) erros.add("Não achei a coluna do fornecedor/comprador (use o cabeçalho: codigo_fornecedor).");
        if (!csv.temColuna(CODIGO_SERVICO)) erros.add("Não achei a coluna do serviço (use o cabeçalho: codigo_servico).");
        if (erros.total > 0) {
            return new Relatorio("vínculos", simular, false, csv.linhas().size(), 0, 0, 0, 0, 0, erros.lista, erros.total, avisos.lista);
        }

        Map<String, Fornecedor> fornecedores = new HashMap<>();
        for (Fornecedor f : fornecedorRepository.findAll()) {
            if (f.getCodigoFornecedor() != null) fornecedores.put(f.getCodigoFornecedor(), f);
        }
        Map<String, StatusServico> statusPorCodigo = new HashMap<>();
        for (ServicoIndiceView v : servicoRepository.listarParaIndice()) statusPorCodigo.put(v.getCodigoServico(), v.getStatus());
        // [PREFIXO] aceita o código completo (CIV-4057137) ou o código SAP sem prefixo (4057137), se for único
        Map<String, String> porCodigoSemPrefixo = new HashMap<>();
        Set<String> ambiguos = new HashSet<>();
        for (String completo : statusPorCodigo.keySet()) {
            int traco = completo.indexOf('-');
            if (traco <= 0) continue;
            if (porCodigoSemPrefixo.put(completo.substring(traco + 1), completo) != null) ambiguos.add(completo.substring(traco + 1));
        }
        Set<Par> existentes = new HashSet<>();
        for (ParVinculoView p : fornecedorServicoRepository.listarPares()) existentes.add(new Par(p.getFornecedor(), p.getServico()));

        Map<String, Fornecedor> novosFornecedores = new LinkedHashMap<>();
        Set<Par> novosPares = new LinkedHashSet<>();
        int jaExistiam = 0;
        int repetidas = 0;

        for (CsvLeitor.Linha l : csv.linhas()) {
            String ref = "Linha " + l.numero() + ": ";
            String cf = limparCodigo(l.get(CODIGO_FORNECEDOR));
            String cs = limparCodigo(l.get(CODIGO_SERVICO));
            if (cf.isEmpty() || cs.isEmpty()) { erros.add(ref + "código do fornecedor ou do serviço vazio."); continue; }

            if (!statusPorCodigo.containsKey(cs) && porCodigoSemPrefixo.containsKey(cs) && !ambiguos.contains(cs)) {
                cs = porCodigoSemPrefixo.get(cs);
            }
            StatusServico status = statusPorCodigo.get(cs);
            if (status == null) { erros.add(ref + "o serviço " + cs + " não existe (importe os serviços antes)."); continue; }
            if (status != StatusServico.APROVADO) {
                erros.add(ref + "o serviço " + cs + " está " + status + ": só serviço APROVADO pode ser vinculado.");
                continue;
            }
            Par par = new Par(cf, cs);
            if (existentes.contains(par)) { jaExistiam++; continue; }
            if (!novosPares.add(par)) { repetidas++; continue; }
            if (!fornecedores.containsKey(cf) && !novosFornecedores.containsKey(cf)) novosFornecedores.put(cf, new Fornecedor(cf));
        }

        if (repetidas > 0) avisos.add(repetidas + " vínculo(s) repetido(s) no arquivo foram ignorados.");
        if (jaExistiam > 0) avisos.add(jaExistiam + " vínculo(s) já existiam e foram pulados.");

        boolean gravar = erros.total == 0 && !simular;
        if (gravar) {
            for (Fornecedor f : novosFornecedores.values()) em.persist(f);
            fornecedores.putAll(novosFornecedores);
            em.flush();   // garante que os fornecedores novos já têm id
            // [PERFORMANCE] FornecedorServico usa id IDENTITY, e com IDENTITY o Hibernate NÃO agrupa os INSERTs:
            // eram ~9 mil idas e voltas ao banco (muito lento no Supabase). Aqui vai tudo em lotes JDBC de 500.
            final List<Par> pares = new ArrayList<>(novosPares);
            final Map<String, Fornecedor> porCodigo = fornecedores;
            em.unwrap(org.hibernate.Session.class).doWork(conexao -> {
                try (java.sql.PreparedStatement ps = conexao.prepareStatement(
                        "insert into fornecedor_servico (fornecedor_id, codigo_servico) values (?, ?)")) {
                    int n = 0;
                    for (Par p : pares) {
                        ps.setLong(1, porCodigo.get(p.fornecedor()).getId());
                        ps.setString(2, p.servico());
                        ps.addBatch();
                        if (++n % LOTE == 0) ps.executeBatch();
                    }
                    ps.executeBatch();
                }
            });
        }
        return new Relatorio("vínculos", simular, gravar, csv.linhas().size(), novosPares.size(), jaExistiam, repetidas,
                0, novosFornecedores.size(), erros.lista, erros.total, avisos.lista);
    }

    /** Excel às vezes exporta números como "4033776.0": tira o ".0" e os espaços. */
    private static String limparCodigo(String bruto) {
        String c = bruto == null ? "" : bruto.strip();
        return c.matches("\\d+\\.0+") ? c.substring(0, c.indexOf('.')) : c;
    }
}
