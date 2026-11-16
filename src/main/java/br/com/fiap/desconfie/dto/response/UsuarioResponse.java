package br.com.fiap.desconfie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Dados devolvidos ao front. Não tem o campo senha: ela nunca sai da API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String status;
    private LocalDate dataCadastro;
}
