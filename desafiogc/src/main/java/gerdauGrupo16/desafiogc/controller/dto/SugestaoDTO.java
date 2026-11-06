package gerdauGrupo16.desafiogc.controller.dto;

/*
 * ARQUIVO: SugestaoDTO.java
 * TIPO: record (objeto de transferência de dados).
 * FUNÇÃO: uma sugestão de serviço parecido, mostrada ao comprador.
 * CAMPOS: codigoServico, nomeServico e aderenciaPercentual (0 a 100, com uma casa decimal).
 * MÉTODOS:
 * - de(codigo, nome, nota): cria a sugestão a partir da nota da régua (0 a 1), convertendo para percentual
 *   com uma casa decimal.
 */

public record SugestaoDTO(String codigoServico, String nomeServico, double aderenciaPercentual) {

    public static SugestaoDTO de(String codigo, String nome, double nota) {
        return new SugestaoDTO(codigo, nome, Math.round(nota * 1000) / 10.0);
    }
}
