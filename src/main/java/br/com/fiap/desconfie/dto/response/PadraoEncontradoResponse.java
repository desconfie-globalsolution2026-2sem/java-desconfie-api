package br.com.fiap.desconfie.dto.response;

import br.com.fiap.desconfie.enums.TipoPadrao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PadraoEncontradoResponse {
    private Long id;
    private String termo;
    private TipoPadrao tipo;
    private String categoria;
    private Integer peso;
    private Integer ocorrencias;
}
