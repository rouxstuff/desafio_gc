package gerdauGrupo16.desafiogc.controller.dto;

//Um serviço existente parecido com o que o comprador tentou criar.
public record SugestaoDTO(String codigoServico, String nomeServico, double aderenciaPercentual) {

    // nota de 0.0 a 1.0 vira percentual com uma casa decimal (0.9091 -> 90.9).
    public static SugestaoDTO de(String codigo, String nome, double nota) {
        return new SugestaoDTO(codigo, nome, Math.round(nota * 1000) / 10.0);
    }
}
