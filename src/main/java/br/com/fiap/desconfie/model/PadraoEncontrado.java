package br.com.fiap.desconfie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Um padrão encontrado em uma análise (tabela associativa N:N tb_dsf_analise_padrao).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PadraoEncontrado {
    private PadraoGolpe padrao;   // cd_padrao
    private int ocorrencias;      // nr_ocorrencias: quantas vezes apareceu
}
