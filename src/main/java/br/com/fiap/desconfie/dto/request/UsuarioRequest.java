package br.com.fiap.desconfie.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados recebidos no cadastro (POST /usuarios).
 * As anotações conferem o FORMATO; as regras de negócio (e-mail repetido) ficam no service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 80, message = "O nome deve ter entre 3 e 80 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 60, message = "A senha deve ter entre 8 e 60 caracteres")
    private String senha;

    // Opcional: vazio ou de 10 a 11 dígitos (DDD + número)
    @Pattern(regexp = "^$|^\\d{10,11}$", message = "O telefone deve ter 10 ou 11 dígitos, só números")
    private String telefone;
}
