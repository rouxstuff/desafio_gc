package gerdauGrupo16.desafiogc.repository;

/*
 * ARQUIVO: ServicoOpcaoView.java
 * TIPO: interface de projeção (leitura leve de duas colunas).
 * FUNÇÃO: item das listas suspensas e da busca da tela de vínculos (só código e nome).
 * MÉTODOS:
 * - getCodigoServico(): código.
 * - getNomeServico(): nome.
 */

public interface ServicoOpcaoView {
    String getCodigoServico();
    String getNomeServico();
}
