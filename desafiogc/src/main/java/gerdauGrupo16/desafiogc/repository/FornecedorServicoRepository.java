package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.FornecedorServico;
import org.springframework.data.jpa.repository.JpaRepository;
import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FornecedorServicoRepository extends JpaRepository<FornecedorServico, Long> {

    boolean existsByFornecedor_IdAndServico_CodigoServico(Long fornecedorId, String codigoServico);

    List<FornecedorServico> findByFornecedor_Id(Long fornecedorId);

    List<FornecedorServico> findByServico_CodigoServico(String codigoServico);

    // [PERFORMANCE] serviços de um fornecedor em UMA query (antes: 1 query por vínculo por causa do EAGER)
    @Query("select s from FornecedorServico fs join fs.servico s "
         + "left join fetch s.categoria left join fetch s.unidadeMedida left join fetch s.unidadeCusto "
         + "where fs.fornecedor.id = :id order by s.nomeServico")
    List<Servico> listarServicosDoFornecedor(@Param("id") Long fornecedorId);

    // [PERFORMANCE] fornecedores de um serviço em UMA query, sem carregar o serviço de novo
    @Query("select fs.fornecedor from FornecedorServico fs where fs.servico.codigoServico = :codigo order by fs.fornecedor.id")
    List<Fornecedor> listarFornecedoresDoServico(@Param("codigo") String codigoServico);

    @Query("select f.codigoFornecedor as fornecedor, s.codigoServico as servico "
         + "from FornecedorServico fs join fs.fornecedor f join fs.servico s")
    List<ParVinculoView> listarPares();

}
