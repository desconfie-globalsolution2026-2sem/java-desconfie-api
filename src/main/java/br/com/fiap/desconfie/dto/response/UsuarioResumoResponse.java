package br.com.fiap.desconfie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados mínimos de um usuário, usados dentro de outras respostas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumoResponse {
    private Long id;
    private String nome;
    private String email;
}
