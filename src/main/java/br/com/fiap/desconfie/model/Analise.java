package br.com.fiap.desconfie.model;

import br.com.fiap.desconfie.enums.Feedback;
import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.enums.TipoEntrada;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Uma verificação feita por um usuário (tabela tb_dsf_analise).
 * N:1 com usuário, N:N com padrões (via PadraoEncontrado) e 1:N com alertas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Analise {
    private Long id;                   // cd_analise
    private Usuario usuario;           // cd_usuario (quem pediu a análise)
    private TipoEntrada tipo;          // tp_entrada
    private String conteudo;           // ds_conteudo (texto, texto lido do print ou URL)
    private Double notaRegras;         // nr_nota_regras (Java)
    private Double notaIa;             // nr_nota_ia (Python); null = IA indisponível
    private Double notaFinal;          // nr_nota_final
    private NivelRisco risco;          // st_risco
    private Feedback feedback;         // st_feedback
    private LocalDateTime dataAnalise; // dt_analise
    private List<PadraoEncontrado> padroesEncontrados = new ArrayList<>();
    private String categoriaPrincipal; // calculada (categoria do padrão de maior peso), não é coluna
}
