package br.com.fiap.desconfie.controller;

import br.com.fiap.desconfie.dto.response.AlertaResponse;
import br.com.fiap.desconfie.service.AlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rotas dos alertas recebidos pelos membros do círculo.
 */
@RestController
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    /** GET /usuarios/{id}/alertas?naoLidos=true -> alertas recebidos por este usuário. */
    @GetMapping("/usuarios/{id}/alertas")
    public ResponseEntity<List<AlertaResponse>> listar(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "false") boolean naoLidos) {
        return ResponseEntity.ok(alertaService.listar(id, naoLidos));
    }

    /** PATCH /alertas/{id}/lido -> marca o alerta como visto. */
    @PatchMapping("/alertas/{id}/lido")
    public ResponseEntity<AlertaResponse> marcarComoLido(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.marcarComoLido(id));
    }
}
