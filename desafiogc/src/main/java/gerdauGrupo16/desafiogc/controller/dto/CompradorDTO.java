package gerdauGrupo16.desafiogc.controller.dto;

/*
 * ARQUIVO: CompradorDTO.java
 * TIPO: record (objeto de transferência de dados).
 * FUNÇÃO: formato simplificado de comprador (código e nome).
 * CAMPOS: codigoComprador e nomeComprador.
 */

public record CompradorDTO(String codigoComprador,String nomeComprador) {
}
