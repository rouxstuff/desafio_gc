package gerdauGrupo16.desafiogc.config;

import gerdauGrupo16.desafiogc.model.Servico;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CargaInicial implements CommandLineRunner {

    private final ServicoRepository repository;

    public CargaInicial(ServicoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    new Servico("4090071", "ADMINISTRATIVO"),
                    new Servico("4096348", "AJUDANTE"),
                    new Servico("4047850", "MANUTENCAO"),
                    new Servico("4050505", "ELETRICISTA")
            ));
        }
    }
}
