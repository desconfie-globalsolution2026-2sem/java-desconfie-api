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
public class PadraoResponse {
    private Long id;
    private Long categoriaId;      // null para DOMINIO_OFICIAL
    private String categoriaNome;
    private TipoPadrao tipo;
    private String termo;
    private Integer peso;
    private Boolean ativo;
}
