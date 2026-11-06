package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: ServicoIndiceView.java
 * TIPO: interface de projeção (leitura leve de quatro colunas).
 * FUNÇÃO: dados mínimos de cada serviço para o índice em memória e para a validação da importação.
 * MÉTODOS:
 * - getCodigoServico(): código.
 * - getNomeServico(): nome.
 * - getCategoriaId(): id da categoria.
 * - getStatus(): situação do serviço.
 */

import gerdauGrupo16.desafiogc.model.enumeration.StatusServico;

public interface ServicoIndiceView {
    String getCodigoServico();
    String getNomeServico();
    Long getCategoriaId();
    StatusServico getStatus();
}
