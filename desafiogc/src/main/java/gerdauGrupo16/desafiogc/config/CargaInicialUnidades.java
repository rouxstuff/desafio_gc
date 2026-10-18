package gerdauGrupo16.desafiogc.config;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import gerdauGrupo16.desafiogc.repository.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CargaInicialUnidades implements CommandLineRunner {

    private final UnidadeCustoRepository custoRepository;
    private final UnidadeMedidaRepository medidaRepository;
    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;

    public CargaInicialUnidades(UnidadeCustoRepository custoRepository,
                                UnidadeMedidaRepository medidaRepository,
                                ServicoRepository servicoRepository,
                                CategoriaRepository categoriaRepository,
                                FornecedorRepository fornecedorRepository) {
        this.custoRepository = custoRepository;
        this.medidaRepository = medidaRepository;
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Override
    @NullMarked
    public void run(String... args) {
        if (fornecedorRepository.count() == 0){
            fornecedorRepository.saveAll(List.of(
               new Fornecedor(UUID.randomUUID().toString()),
               new Fornecedor(UUID.randomUUID().toString()),
               new Fornecedor(UUID.randomUUID().toString())
            ));
        }

        if (categoriaRepository.count() == 0) {
            categoriaRepository.saveAll(List.of(
                    new Categoria("Civil".toUpperCase()),
                    new Categoria("Eletromecânica".toUpperCase()),
                    new Categoria("Facilities".toUpperCase())
            ));
        }

        if (servicoRepository.count() == 0) {
            servicoRepository.saveAll(List.of(
                    new Servico("4090071", "ADMINISTRATIVO"),
                    new Servico("4096348", "AJUDANTE"),
                    new Servico("4047850", "MANUTENCAO"),
                    new Servico("4050505", "ELETRICISTA")
            ));
        }

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
    }
}
