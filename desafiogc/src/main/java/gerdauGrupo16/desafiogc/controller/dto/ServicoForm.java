package gerdauGrupo16.desafiogc.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * [NOVO] Objeto que o formulário HTML (Thymeleaf) preenche.
 *
 * Por que não usar o ServicoDTO direto? O Thymeleaf liga cada campo da tela a um getter/setter
 * (th:field), e um record não tem setters. Esta classe é só a "ponte" entre a tela e o ServicoDTO.
 * As validações são as mesmas do ServicoDTO.
 */
@Getter
@Setter
public class ServicoForm {

    @NotBlank(message = "O código é obrigatório.")
    @Size(max = 20, message = "O código deve ter no máximo 20 caracteres.")
    private String codigoServico;

    @NotBlank(message = "O nome do serviço é obrigatório.")
    @Size(max = 40, message = "O nome deve ter no máximo 40 caracteres (limite do SAP).")
    private String nomeServico;

    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres.")
    private String descricaoServico;

    @NotNull(message = "Escolha a categoria.")
    private Long categoriaId;

    private Long unidadeMedidaId;

    private Long unidadeCustoId;

    /** Converte para o DTO que o ServicoService já entende. Descrição vazia vira null. */
    public ServicoDTO paraDto() {
        String descricao = (descricaoServico == null || descricaoServico.isBlank()) ? null : descricaoServico;
        return new ServicoDTO(codigoServico, nomeServico, descricao, categoriaId, unidadeMedidaId, unidadeCustoId);
    }
}
