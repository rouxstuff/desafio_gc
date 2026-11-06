package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: CompradorServicoRepository.java
 * TIPO: interface de repositório (Spring Data JPA) da entidade CompradorServico.
 * FUNÇÃO: consultas dos vínculos comprador x serviço. Várias usam JPQL próprias para evitar uma consulta
 *   por vínculo (problema N+1).
 * MÉTODOS:
 * - existsByComprador_IdAndServico_CodigoServico(compradorId, codigoServico): diz se o vínculo já existe.
 * - findByComprador_Id(compradorId): vínculos de um comprador.
 * - findByServico_CodigoServico(codigoServico): vínculos de um serviço.
 * - listarServicosDoComprador(compradorId): serviços do comprador, já com categoria e unidades
 *   carregadas, por nome.
 * - listarVinculosDoServico(codigoServico): vínculos do serviço com o comprador já carregado (mostra a data).
 * - listarCompradoresDoServico(codigoServico): compradores que atendem o serviço.
 * - listarPares(): todos os pares (código do comprador, código do serviço), de forma leve. Usado pela
 *   importação para não repetir vínculo.
 */

import gerdauGrupo16.desafiogc.model.entity.CompradorServico;
import org.springframework.data.jpa.repository.JpaRepository;
import gerdauGrupo16.desafiogc.model.entity.Comprador;
import gerdauGrupo16.desafiogc.model.entity.Servico;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompradorServicoRepository extends JpaRepository<CompradorServico, Long> {

    boolean existsByComprador_IdAndServico_CodigoServico(Long compradorId, String codigoServico);

    List<CompradorServico> findByComprador_Id(Long compradorId);

    List<CompradorServico> findByServico_CodigoServico(String codigoServico);

    @Query("select s from CompradorServico fs join fs.servico s "
         + "left join fetch s.categoria left join fetch s.unidadeMedida left join fetch s.unidadeCusto "
         + "where fs.comprador.id = :id order by s.nomeServico")
    List<Servico> listarServicosDoComprador(@Param("id") Long compradorId);

    @Query("select fs from CompradorServico fs join fetch fs.comprador where fs.servico.codigoServico = :codigo order by fs.comprador.id")
    List<CompradorServico> listarVinculosDoServico(@Param("codigo") String codigoServico);

    @Query("select fs.comprador from CompradorServico fs where fs.servico.codigoServico = :codigo order by fs.comprador.id")
    List<Comprador> listarCompradoresDoServico(@Param("codigo") String codigoServico);

    @Query("select f.codigoComprador as comprador, s.codigoServico as servico "
         + "from CompradorServico fs join fs.comprador f join fs.servico s")
    List<ParVinculoView> listarPares();

}
