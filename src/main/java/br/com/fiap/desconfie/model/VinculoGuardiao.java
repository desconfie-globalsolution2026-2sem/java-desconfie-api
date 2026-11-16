package br.com.fiap.desconfie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Ligação de proteção entre dois usuários (tabela tb_dsf_vinculo_guardiao).
 * Um guardião protege até 3 dependentes; cada dependente tem um único guardião.
 * Guardião e dependentes formam o "círculo", que compartilha histórico e alertas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VinculoGuardiao {
    private Usuario guardiao;      // cd_guardiao
    private Usuario dependente;    // cd_dependente
    private LocalDate dataVinculo; // dt_vinculo
}
