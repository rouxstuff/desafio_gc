package gerdauGrupo16.desafiogc.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** O código do serviço NÃO vem mais do usuário: o sistema gera (ex.: FAC-000001). */
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
