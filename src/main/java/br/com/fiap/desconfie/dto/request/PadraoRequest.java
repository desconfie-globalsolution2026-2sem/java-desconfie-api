package br.com.fiap.desconfie.dto.request;

import br.com.fiap.desconfie.enums.TipoPadrao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados recebidos no cadastro e na atualização de padrão.
 * categoriaId é ignorado na rota /categorias/{id}/padroes (vale o id da URL)
 * e deve ficar vazio para DOMINIO_OFICIAL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PadraoRequest {

    private Long categoriaId;

    @NotNull(message = "O tipo é obrigatório (TEXTO, TLD, ENCURTADOR, MARCA ou DOMINIO_OFICIAL)")
    private TipoPadrao tipo;

    @NotBlank(message = "O termo é obrigatório")
    @Size(max = 120, message = "O termo deve ter no máximo 120 caracteres")
    private String termo;

    @NotNull(message = "O peso é obrigatório")
    @Min(value = 0, message = "O peso mínimo é 0")
    @Max(value = 50, message = "O peso máximo é 50")
    private Integer peso;

    // opcional: se não vier, o padrão é criado ativo
    private Boolean ativo;
}
