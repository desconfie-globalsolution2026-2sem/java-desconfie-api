package br.com.fiap.desconfie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Um membro do círculo (dependente ou guardião) com a data do vínculo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembroCirculoResponse {
    private Long id;
    private String nome;
    private String email;
    private LocalDate dataVinculo;
}
