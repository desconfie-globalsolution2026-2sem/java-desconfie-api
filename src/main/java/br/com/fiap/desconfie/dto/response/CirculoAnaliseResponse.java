package br.com.fiap.desconfie.dto.response;

import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.enums.TipoEntrada;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Análise amarela ou vermelha compartilhada com o círculo (inclui quem analisou e o conteúdo). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CirculoAnaliseResponse {
    private Long id;
    private AutorResponse autor;
    private TipoEntrada tipo;
    private String conteudo;
    private Double notaFinal;
    private NivelRisco risco;
    private String categoriaPrincipal;
    private LocalDateTime dataAnalise;
}
