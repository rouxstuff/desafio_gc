package gerdauGrupo16.desafiogc.controller.dto;

/*
 * ARQUIVO: FornecedorDTO.java
 * TIPO: record (objeto de transferência de dados).
 * FUNÇÃO: formato simplificado de fornecedor (código e nome).
 * CAMPOS: codigoFornecedor e nomeFornecedor.
 */

public record FornecedorDTO(String codigoFornecedor,String nomeFornecedor) {
}
