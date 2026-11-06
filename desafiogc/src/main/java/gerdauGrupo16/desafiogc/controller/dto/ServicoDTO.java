package gerdauGrupo16.desafiogc.controller.dto;

/*
 * ARQUIVO: ServicoDTO.java
 * TIPO: record (objeto de transferência de dados) com validação.
 * FUNÇÃO: dados de entrada para criar um serviço novo. As anotações validam antes de chegar ao service.
 * CAMPOS: nomeServico (obrigatório, até 40 caracteres, limite do SAP); descricaoServico (opcional, até
 *   100); categoriaId (obrigatório); unidadeMedidaId e unidadeCustoId (opcionais).
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServicoDTO(
        @NotBlank(message = "O nome do serviço é obrigatório.")
        @Size(max = 40, message = "O nome deve ter no máximo 40 caracteres (limite do SAP).")
        String nomeServico,
        @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres.")
        String descricaoServico,
        @NotNull(message = "A categoria é obrigatória.")
        Long categoriaId,
        Long unidadeMedidaId,
        Long unidadeCustoId)
{ }
