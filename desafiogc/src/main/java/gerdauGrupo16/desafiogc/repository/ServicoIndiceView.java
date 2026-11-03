package gerdauGrupo16.desafiogc.repository;

import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;

/** Projeção leve (só 4 colunas) para montar o índice em memória e validar importações, sem carregar entidades inteiras. */
public interface ServicoIndiceView {
    String getCodigoServico();
    String getNomeServico();
    Long getCategoriaId();
    StatusServico getStatus();
}
