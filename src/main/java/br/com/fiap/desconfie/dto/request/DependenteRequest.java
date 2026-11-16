package br.com.fiap.desconfie.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O guardião adiciona um dependente pelo e-mail de quem já tem conta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DependenteRequest {

    @NotBlank(message = "O e-mail do dependente é obrigatório")
    @Email(message = "E-mail inválido")
    private String emailDependente;
}
