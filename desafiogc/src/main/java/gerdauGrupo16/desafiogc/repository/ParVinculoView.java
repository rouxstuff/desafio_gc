package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: ParVinculoView.java
 * TIPO: interface de projeção (leitura leve de duas colunas).
 * FUNÇÃO: um par fornecedor + serviço sem carregar as entidades.
 * MÉTODOS:
 * - getFornecedor(): código do fornecedor.
 * - getServico(): código do serviço.
 */

public interface ParVinculoView {
    String getFornecedor();
    String getServico();
}
