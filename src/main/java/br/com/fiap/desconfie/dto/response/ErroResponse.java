package br.com.fiap.desconfie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato único de todos os erros da API (4xx e 5xx).
 * Exemplo: { "status": 409, "erro": "Conflict", "mensagem": "...", "caminho": "/usuarios",
 *            "timestamp": "2026-11-15T14:32:10", "campos": null }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErroResponse {
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private LocalDateTime timestamp;
    private List<CampoErro> campos;   // preenchido só nos erros de validação (400)
}
