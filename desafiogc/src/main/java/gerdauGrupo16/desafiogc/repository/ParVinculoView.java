package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: ParVinculoView.java
 * TIPO: interface de projeção (leitura leve de duas colunas).
 * FUNÇÃO: um par comprador + serviço sem carregar as entidades.
 * MÉTODOS:
 * - getComprador(): código do comprador.
 * - getServico(): código do serviço.
 */

public interface ParVinculoView {
    String getComprador();
    String getServico();
}
