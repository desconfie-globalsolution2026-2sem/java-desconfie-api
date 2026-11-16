package br.com.fiap.desconfie.model;

import br.com.fiap.desconfie.enums.TipoPadrao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Frase, termo ou sinal de link com peso (tabela tb_dsf_padrao_golpe).
 * Pertence a uma categoria (N:1), exceto DOMINIO_OFICIAL, que não tem categoria.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PadraoGolpe {
    private Long id;                  // cd_padrao
    private CategoriaGolpe categoria; // cd_categoria (objeto, não só o id)
    private TipoPadrao tipo;          // tp_padrao
    private String termo;             // ds_termo (guardado sem acento e em minúsculas)
    private Integer peso;             // nr_peso (0 a 50)
    private Boolean ativo;            // st_ativo: S/N
}
