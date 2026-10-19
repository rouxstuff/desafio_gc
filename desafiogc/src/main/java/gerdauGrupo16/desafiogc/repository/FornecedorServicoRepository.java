package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.FornecedorServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FornecedorServicoRepository extends JpaRepository<FornecedorServico, Long> {

    boolean existsByFornecedor_IdAndServico_CodigoServico(Long fornecedorId, String codigoServico);

    List<FornecedorServico> findByFornecedor_Id(Long fornecedorId);
}
