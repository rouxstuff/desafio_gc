package gerdauGrupo16.desafiogc.controller.dto;

/*
 * ARQUIVO: UnidadeCustoDTO.java
 * TIPO: record (objeto de transferência de dados).
 * FUNÇÃO: formato de resposta da API de unidades de custo.
 * CAMPOS: id e maoDeObra (o nome do adicional).
 */

public record UnidadeCustoDTO(Long id, String maoDeObra) {
}
