package br.com.fiap.desconfie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Quem fez a análise (usado no histórico do círculo e nos alertas). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutorResponse {
    private Long id;
    private String nome;
    private String telefone;
}
