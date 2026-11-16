package br.com.fiap.desconfie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Resposta do POST /usuarios/{id}/dependentes.
 * Ex.: { "guardiaoId": 1, "dependente": { "id": 9, "nome": "...", "email": "..." }, "dataVinculo": "2026-11-15" }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VinculoResponse {
    private Long guardiaoId;
    private UsuarioResumoResponse dependente;
    private LocalDate dataVinculo;
}
