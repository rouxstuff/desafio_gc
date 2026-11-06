package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: FornecedorServicoRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade FornecedorServico.
 * FUNÇÃO: consultas dos vínculos fornecedor x serviço. Várias usam JPQL próprias para evitar uma consulta
 *   por vínculo (problema N+1).
 * MÉTODOS:
 * - existsByFornecedor_IdAndServico_CodigoServico(fornecedorId, codigoServico): diz se o vínculo já existe.
 * - findByFornecedor_Id(fornecedorId): vínculos de um fornecedor.
 * - findByServico_CodigoServico(codigoServico): vínculos de um serviço.
 * - listarServicosDoFornecedor(fornecedorId): serviços do fornecedor, já com categoria e unidades
 *   carregadas, por nome.
 * - listarFornecedoresDoServico(codigoServico): fornecedores que atendem o serviço.
 * - listarPares(): todos os pares (código do fornecedor, código do serviço), de forma leve. Usado pela
 *   importação para não repetir vínculo.
 */

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

    @Query("select s from FornecedorServico fs join fs.servico s "
         + "left join fetch s.categoria left join fetch s.unidadeMedida left join fetch s.unidadeCusto "
         + "where fs.fornecedor.id = :id order by s.nomeServico")
    List<Servico> listarServicosDoFornecedor(@Param("id") Long fornecedorId);

    @Query("select fs.fornecedor from FornecedorServico fs where fs.servico.codigoServico = :codigo order by fs.fornecedor.id")
    List<Fornecedor> listarFornecedoresDoServico(@Param("codigo") String codigoServico);

    @Query("select f.codigoFornecedor as fornecedor, s.codigoServico as servico "
         + "from FornecedorServico fs join fs.fornecedor f join fs.servico s")
    List<ParVinculoView> listarPares();

}
