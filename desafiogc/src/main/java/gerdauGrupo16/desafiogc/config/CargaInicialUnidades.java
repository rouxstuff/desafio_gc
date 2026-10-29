package gerdauGrupo16.desafiogc.config;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.FornecedorServico;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.repository.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CargaInicialUnidades implements CommandLineRunner {

    private final UnidadeCustoRepository custoRepository;
    private final UnidadeMedidaRepository medidaRepository;
    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;
    // [NOVO] para o exemplo de vínculo fornecedor <-> serviço
    private final FornecedorServicoRepository fornecedorServicoRepository;

    public CargaInicialUnidades(UnidadeCustoRepository custoRepository,
                                UnidadeMedidaRepository medidaRepository,
                                ServicoRepository servicoRepository,
                                CategoriaRepository categoriaRepository,
                                FornecedorRepository fornecedorRepository,
                                FornecedorServicoRepository fornecedorServicoRepository) {
        this.custoRepository = custoRepository;
        this.medidaRepository = medidaRepository;
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.fornecedorServicoRepository = fornecedorServicoRepository;
    }

    @Override
    @NullMarked
    public void run(String... args) {
        // 1) Tabelas de apoio (oficiais da Gerdau): unidades de medida e de custo
        if (medidaRepository.count() == 0) {
            medidaRepository.saveAll(List.of(
                    new UnidadeMedida("CM", "Centímetro"),
                    new UnidadeMedida("CDM", "Centímetro Cúbico"),
                    new UnidadeMedida("ZCJ", "Conjunto"),
                    new UnidadeMedida("TAG", "Dia"),
                    new UnidadeMedida("H", "Hora"),
                    new UnidadeMedida("L", "Litro"),
                    new UnidadeMedida("MON", "Mês"),
                    new UnidadeMedida("M", "Metro"),
                    new UnidadeMedida("M3", "Metro cúbico"),
                    new UnidadeMedida("M2", "Metro quadrado"),
                    new UnidadeMedida("ML", "Mililitro"),
                    new UnidadeMedida("MM", "Milímetro"),
                    new UnidadeMedida("KG", "Quilograma"),
                    new UnidadeMedida("KM", "Quilômetro"),
                    new UnidadeMedida("SV", "Serviço"),
                    new UnidadeMedida("TO", "Tonelada"),
                    new UnidadeMedida("ST", "Unidade (peça)")));
        }

        if (custoRepository.count() == 0) {
            custoRepository.saveAll(List.of(
                    new UnidadeCusto("Hora normal", "Hora de trabalho em jornada padrão, sem nenhum adicional aplicado — a hora 'base' da função."),
                    new UnidadeCusto("Hora extra Seg a Sab", "Hora trabalhada além da jornada normal em dia de semana ou sábado, sujeita ao percentual de hora extra definido em contrato (normalmente 50%/75%, variável por fornecedor)"),
                    new UnidadeCusto("Hora extra Dom/Feriado", "Hora trabalhada além da jornada normal em domingo ou feriado, geralmente com percentual maior de adicional (normalmente 100%)."),
                    new UnidadeCusto("Adicional noturno (hora)", "Acréscimo pago sobre a hora normal quando o trabalho ocorre no período noturno (tipicamente 22h–5h), sem ser hora extra."),
                    new UnidadeCusto("Hora extra noturna Seg a Sab", "Combinação de hora extra (dia de semana/sábado) com o adicional noturno — para quando a extensão de jornada cai no período noturno."),
                    new UnidadeCusto("Hora extra noturna Dom/Feriado", "Combinação de hora extra em domingo/feriado com o adicional noturno — o cenário de maior acréscimo entre os de hora extra."),
                    new UnidadeCusto("Adicional periculosidade (hora)", "Acréscimo pago por hora trabalhada em atividade classificada como perigosa (ex: eletricidade, inflamáveis), aplicado sobre a hora normal."),
                    new UnidadeCusto("Adicional periculosidade s/ hora extra", "Mesmo adicional de periculosidade, mas aplicado especificamente sobre horas extras (para diferenciar do adicional sobre hora normal, já que a base de cálculo muda)."),
                    new UnidadeCusto("Adicional periculosidade (mensal)", "Mesmo adicional de periculosidade, mas cobrado como valor fixo mensal em vez de por hora — usado quando o fornecedor fatura por posto fixo."),
                    new UnidadeCusto("Adicional insalubridade (hora)", "Acréscimo pago por hora quando a atividade expõe o trabalhador a agentes nocivos à saúde (ruído, calor, produtos químicos, etc.), com grau variável (mínimo/médio/máximo)."),
                    new UnidadeCusto("Posto mensal (equipe fixa)", "Item de cobrança para uma função alocada em regime de posto fixo/mensalista, faturado por mês em vez de por hora — comum em contratos de equipe residente."),
                    new UnidadeCusto("Mobilizacao / Desmobilizacao", "Cobrança única referente ao deslocamento, instalação e retirada da equipe/função no início e no fim do contrato ou de uma frente de trabalho (não é hora trabalhada, é custo de logística)."),
                    new UnidadeCusto("Retroativo", "É um item de ajuste financeiro (pagamento retroativo por reclassificação, correção de tarifa, migração de sistema, etc.)"),
                    new UnidadeCusto("Itens que não são mão de obra", "Todo o restante, como materiais aplicados nos serviços, equipamentos, ferramentas, veículos, custos indiretos, transporte etc.")));
        }

        // 2) Categorias (os 3 grupos de serviço da base)
        if (categoriaRepository.count() == 0) {
            categoriaRepository.saveAll(List.of(
                    new Categoria("Civil".toUpperCase()),
                    new Categoria("Eletromecânica".toUpperCase()),
                    new Categoria("Facilities".toUpperCase())
            ));
        }

        // 3) Fornecedores FICTÍCIOS (a base real é anonimizada). Todos com código e nome preenchidos.
        if (fornecedorRepository.count() == 0) {
            fornecedorRepository.saveAll(List.of(
                    fornecedor("F001", "Alfa Engenharia"),
                    fornecedor("F002", "Beta Serviços Industriais"),
                    fornecedor("F003", "Gama Construções"),
                    fornecedor("F004", "Delta Montagens"),
                    fornecedor("F005", "Ômega Facilities"),
                    fornecedor("F006", "Sigma Terceirização")   // começa SEM vínculos, para vincular ao vivo
            ));
        }

        // 4) Serviços. Nome = só a função (sem adicionais), em MAIÚSCULO e sem acento, até 40 caracteres.
        if (servicoRepository.count() == 0) {
            Categoria civil = buscarCategoria("CIVIL");
            Categoria eletromecanica = buscarCategoria("ELETROMEC");
            Categoria facilities = buscarCategoria("FACILITIES");
            UnidadeMedida h = medida("H"), m = medida("M"), st = medida("ST"), sv = medida("SV"), mon = medida("MON");
            UnidadeCusto periculosidade = custo("Adicional periculosidade (hora)");
            UnidadeCusto postoMensal = custo("Posto mensal (equipe fixa)");

            servicoRepository.saveAll(List.of(
                    // --- os 4 originais do seed, agora com medida e descrição ---
                    servico("4090071", "ADMINISTRATIVO", "Auxiliar de rotina administrativa", facilities, h, null, StatusServico.APROVADO),
                    servico("4096348", "AJUDANTE", "Ajudante de obras civis", civil, h, null, StatusServico.APROVADO),
                    servico("4047850", "MANUTENCAO", "Manutenção geral de equipamentos", eletromecanica, sv, null, StatusServico.APROVADO),
                    servico("4050505", "ELETRICISTA", "Eletricista de manutenção", eletromecanica, h, periculosidade, StatusServico.APROVADO),

                    // --- mesmo nome em outra categoria (a tela pede para escolher) ---
                    servico("4096350", "AJUDANTE", "Ajudante de montagem eletromecânica", eletromecanica, h, null, StatusServico.APROVADO),

                    // --- base para a régua de aderência ---
                    servico("4101010", "MECANICO MONTADOR", "Mecânico de montagem industrial", eletromecanica, h, null, StatusServico.APROVADO),
                    servico("4101020", "TECNICO SEGURANCA TRABALHO JUNIOR", "Técnico de segurança, nível júnior", facilities, h, null, StatusServico.APROVADO),
                    servico("4101030", "VIGIA", "Vigilância em posto fixo, faturada por mês", facilities, mon, postoMensal, StatusServico.APROVADO),
                    servico("4101040", "FURACAO EM CONCRETO COM COROA 1/2", "Furação com coroa diamantada de 1/2 pol", civil, st, null, StatusServico.APROVADO),
                    servico("4101050", "TUBO RIGIDO SOLDAVEL 25MM", "Tubo PVC rígido soldável, bitola 25 mm", civil, m, null, StatusServico.APROVADO),
                    servico("4101051", "TUBO RIGIDO SOLDAVEL 50MM", "Tubo PVC rígido soldável, bitola 50 mm", civil, m, null, StatusServico.APROVADO),
                    servico("4101060", "COFFEE BREAK EVENTO", "Item avulso de Facilities, sem padrão de mão de obra", facilities, sv, null, StatusServico.APROVADO),

                    // --- fluxo de aprovação ---
                    servico("4101070", "ELETRICISTA INDUSTRIAL", "Aguardando aprovação do time de dados", eletromecanica, h, null, StatusServico.PENDENTE),
                    servico("4101080", "AJUDANTE GERAL", "Rejeitado: genérico, usar AJUDANTE", civil, h, null, StatusServico.REJEITADO)
            ));
        }

        // 5) Vínculos fornecedor x serviço (um serviço no catálogo, vários fornecedores)
        if (fornecedorServicoRepository.count() == 0) {
            vincular("F001", "4096348", "4101040");
            vincular("F002", "4096348", "4101040", "4050505");
            vincular("F003", "4101040", "4101050", "4101051");
            vincular("F004", "4101040", "4101010", "4096350");
            vincular("F005", "4050505", "4101030", "4096350");
            // F006 fica sem vínculos de propósito
        }
    }

    // ----------------------------------------------------------------- auxiliares do seed

    private Categoria buscarCategoria(String trechoDoNome) {
        return categoriaRepository.findByCategoriaNomeContaining(trechoDoNome).stream().findFirst().orElse(null);
    }

    private UnidadeMedida medida(String codigo) {
        return medidaRepository.findAll().stream().filter(u -> codigo.equals(u.getCodigo())).findFirst().orElse(null);
    }

    private UnidadeCusto custo(String maoDeObra) {
        return custoRepository.findAll().stream().filter(u -> maoDeObra.equals(u.getMaoDeObra())).findFirst().orElse(null);
    }

    private Fornecedor fornecedor(String codigo, String nome) {
        Fornecedor f = new Fornecedor(codigo);
        f.setNomeFornecedor(nome);
        return f;
    }

    private Servico servico(String codigo, String nome, String descricao, Categoria categoria,
                            UnidadeMedida medida, UnidadeCusto custo, StatusServico status) {
        Servico s = new Servico(codigo, nome);
        s.setDescricaoServico(descricao);
        s.setCategoria(categoria);
        s.setUnidadeMedida(medida);
        s.setUnidadeCusto(custo);
        s.setStatus(status);
        return s;
    }

    private void vincular(String codigoFornecedor, String... codigosServico) {
        Fornecedor f = fornecedorRepository.findByCodigoFornecedor(codigoFornecedor).getFirst();
        for (String codigo : codigosServico) {
            servicoRepository.findById(codigo)
                    .ifPresent(s -> fornecedorServicoRepository.save(new FornecedorServico(f, s)));
        }
    }
}
