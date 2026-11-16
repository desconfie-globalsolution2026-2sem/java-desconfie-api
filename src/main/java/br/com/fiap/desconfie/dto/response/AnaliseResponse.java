package br.com.fiap.desconfie.dto.response;

import br.com.fiap.desconfie.enums.Feedback;
import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.enums.TipoEntrada;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/** Resultado completo de uma análise (POST e GET /analises/{id}). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseResponse {
    private Long id;
    private Long usuarioId;
    private TipoEntrada tipo;
    private String conteudo;
    private Double notaRegras;
    private Double notaIa;
    private Double notaFinal;
    private NivelRisco risco;
    private boolean iaDisponivel;
    private List<PadraoEncontradoResponse> padroesEncontrados;
    private List<SinalLinkResponse> sinaisLink;
    private String categoriaPrincipal;
    private String orientacao;
    private int alertasGerados;
    private Feedback feedback;
    private LocalDateTime dataAnalise;
}
