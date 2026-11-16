package br.com.fiap.desconfie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tipo de golpe (tabela tb_dsf_categoria_golpe), com a orientação mostrada ao usuário.
 * Uma categoria tem vários padrões (1:N).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaGolpe {
    private Long id;            // cd_categoria
    private String nome;        // nm_categoria (único)
    private String descricao;   // ds_categoria
    private String orientacao;  // ds_orientacao
    private List<PadraoGolpe> padroes = new ArrayList<>();

    public CategoriaGolpe(Long id, String nome, String descricao, String orientacao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.orientacao = orientacao;
    }
}
