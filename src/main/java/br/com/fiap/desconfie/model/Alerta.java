package br.com.fiap.desconfie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Aviso gerado para um membro do círculo quando outro membro recebe um VERMELHO (tb_dsf_alerta).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {
    private Long id;                  // cd_alerta
    private Analise analise;          // cd_analise (a análise vermelha, com o autor dentro)
    private Usuario destinatario;     // cd_destinatario (quem recebe o alerta)
    private LocalDateTime dataAlerta; // dt_alerta
    private boolean lido;             // st_lido: S/N
}
