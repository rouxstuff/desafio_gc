package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: CompradorService.java
 * TIPO: classe de serviço (@Service).
 * FUNÇÃO: regras de comprador e do vínculo com serviços.
 * MÉTODOS:
 * - buscarTodos(): todos os compradores.
 * - buscarCodigoComprador(codigoComprador): compradores com o código informado.
 * - vincularServico(compradorId, codigoServico): vincula o comprador a um serviço do catálogo. 404 se
 *   algum não existe; 409 se o serviço não está APROVADO ou se o vínculo já existe.
 * - listarServicosDoComprador(compradorId): serviços do comprador (404 se o comprador não existe).
 */

import gerdauGrupo16.desafiogc.model.entity.Comprador;
import gerdauGrupo16.desafiogc.model.entity.CompradorServico;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.repository.CompradorRepository;
import gerdauGrupo16.desafiogc.repository.CompradorServicoRepository;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CompradorService {

    private final CompradorRepository compradorRepository;

    private final ServicoRepository servicoRepository;
    private final CompradorServicoRepository compradorServicoRepository;

    public CompradorService(CompradorRepository compradorRepository,
                             ServicoRepository servicoRepository,
                             CompradorServicoRepository compradorServicoRepository) {
        this.compradorRepository = compradorRepository;
        this.servicoRepository = servicoRepository;
        this.compradorServicoRepository = compradorServicoRepository;
    }

    public List<Comprador> buscarTodos() {
        return compradorRepository.findAll();
    }

    public List<Comprador> buscarCodigoComprador(String codigoComprador) {
        return compradorRepository.findByCodigoComprador(codigoComprador);
    }

    @Transactional
    public void vincularServico(Long compradorId, String codigoServico) {
        Comprador comprador = compradorRepository.findById(compradorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprador não encontrado."));
        Servico servico = servicoRepository.findById(codigoServico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        if (servico.getStatus() != StatusServico.APROVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O serviço " + codigoServico + " está "
                    + servico.getStatus() + ". Só serviços APROVADOS podem ser vinculados a compradores.");
        }
        if (compradorServicoRepository.existsByComprador_IdAndServico_CodigoServico(compradorId, codigoServico)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este comprador já está vinculado a este serviço.");
        }
        compradorServicoRepository.save(new CompradorServico(comprador, servico));
    }
    @Transactional(readOnly = true)
    public List<Servico> listarServicosDoComprador(Long compradorId) {
        if (!compradorRepository.existsById(compradorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprador não encontrado.");
        }
        return compradorServicoRepository.listarServicosDoComprador(compradorId);
    }

}
