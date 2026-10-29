package gerdauGrupo16.desafiogc.controller.dto;

import gerdauGrupo16.desafiogc.model.entity.Categoria;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeCusto;
import gerdauGrupo16.desafiogc.model.unitys.UnidadeMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServicoDTO(
        @NotBlank(message = "O código é obrigatório.")
        @Size(max = 20, message = "O código deve ter no máximo 20 caracteres.")
        String codigoServico,
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
