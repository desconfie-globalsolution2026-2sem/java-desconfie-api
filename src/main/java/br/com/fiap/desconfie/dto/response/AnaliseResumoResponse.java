package br.com.fiap.desconfie.dto.response;

import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.enums.TipoEntrada;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Linha do histórico (listas de análises). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseResumoResponse {
    private Long id;
    private TipoEntrada tipo;
    private Double notaFinal;
    private NivelRisco risco;
    private String categoriaPrincipal;
    private LocalDateTime dataAnalise;
}
