package br.com.fiap.desconfie.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados recebidos no cadastro e na atualização de categoria.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 60, message = "O nome deve ter entre 3 e 60 caracteres")
    private String nome;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres")
    private String descricao;

    @NotBlank(message = "A orientação é obrigatória")
    @Size(max = 400, message = "A orientação deve ter no máximo 400 caracteres")
    private String orientacao;
}
