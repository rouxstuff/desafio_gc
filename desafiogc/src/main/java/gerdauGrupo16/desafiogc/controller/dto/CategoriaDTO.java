package gerdauGrupo16.desafiogc.controller.dto;

/*
 * ARQUIVO: CategoriaDTO.java
 * TIPO: record (objeto de transferência de dados).
 * FUNÇÃO: formato de resposta da API de categorias.
 * CAMPOS: id e categoriaNome.
 */

import gerdauGrupo16.desafiogc.model.entity.Categoria;

public record CategoriaDTO(Long id, String categoriaNome) {
}
