package gerdauGrupo16.desafiogc.repository;

/** Projeção leve (código + nome) para preencher listas suspensas sem carregar a entidade Servico inteira. */
public interface ServicoOpcaoView {
    String getCodigoServico();
    String getNomeServico();
}
