package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findByCodigoFornecedor(String codigoFornecedor);

}
