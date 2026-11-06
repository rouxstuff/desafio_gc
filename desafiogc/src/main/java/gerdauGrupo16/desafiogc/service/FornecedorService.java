package gerdauGrupo16.desafiogc.service;

/*
 * ARQUIVO: FornecedorService.java
 * TIPO: classe de serviço (@Service).
 * FUNÇÃO: regras de fornecedor e do vínculo com serviços.
 * MÉTODOS:
 * - buscarTodos(): todos os fornecedores.
 * - buscarCodigoFornecedor(codigoFornecedor): fornecedores com o código informado.
 * - vincularServico(fornecedorId, codigoServico): vincula o fornecedor a um serviço do catálogo. 404 se
 *   algum não existe; 409 se o serviço não está APROVADO ou se o vínculo já existe.
 * - listarServicosDoFornecedor(fornecedorId): serviços do fornecedor (404 se o fornecedor não existe).
 */

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.FornecedorServico;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;
import gerdauGrupo16.desafiogc.repository.FornecedorRepository;
import gerdauGrupo16.desafiogc.repository.FornecedorServicoRepository;
import gerdauGrupo16.desafiogc.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    private final ServicoRepository servicoRepository;
    private final FornecedorServicoRepository fornecedorServicoRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository,
                             ServicoRepository servicoRepository,
                             FornecedorServicoRepository fornecedorServicoRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.servicoRepository = servicoRepository;
        this.fornecedorServicoRepository = fornecedorServicoRepository;
    }

    public List<Fornecedor> buscarTodos() {
        return fornecedorRepository.findAll();
    }

    public List<Fornecedor> buscarCodigoFornecedor(String codigoFornecedor) {
        return fornecedorRepository.findByCodigoFornecedor(codigoFornecedor);
    }

    @Transactional
    public void vincularServico(Long fornecedorId, String codigoServico) {
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado."));
        Servico servico = servicoRepository.findById(codigoServico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        if (servico.getStatus() != StatusServico.APROVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O serviço " + codigoServico + " está "
                    + servico.getStatus() + ". Só serviços APROVADOS podem ser vinculados a fornecedores.");
        }
        if (fornecedorServicoRepository.existsByFornecedor_IdAndServico_CodigoServico(fornecedorId, codigoServico)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este fornecedor já está vinculado a este serviço.");
        }
        fornecedorServicoRepository.save(new FornecedorServico(fornecedor, servico));
    }
    @Transactional(readOnly = true)
    public List<Servico> listarServicosDoFornecedor(Long fornecedorId) {
        if (!fornecedorRepository.existsById(fornecedorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado.");
        }
        return fornecedorServicoRepository.listarServicosDoFornecedor(fornecedorId);
    }

}
