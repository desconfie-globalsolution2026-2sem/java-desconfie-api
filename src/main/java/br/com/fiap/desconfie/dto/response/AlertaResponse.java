package br.com.fiap.desconfie.dto.response;

import br.com.fiap.desconfie.enums.NivelRisco;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponse {
    private Long id;
    private Long analiseId;
    private AutorResponse autor;
    private NivelRisco risco;
    private String categoriaPrincipal;
    private Double notaFinal;
    private LocalDateTime dataAlerta;
    private boolean lido;
}
