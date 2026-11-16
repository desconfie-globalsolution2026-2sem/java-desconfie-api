package br.com.fiap.desconfie.controller;

import br.com.fiap.desconfie.service.IaClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GET /health: confirma que a API está no ar e se a IA está respondendo.
 * Abrir esta rota antes de apresentar "acorda" os serviços no Render.
 */
@RestController
public class HealthController {

    private final IaClient iaClient;

    public HealthController(IaClient iaClient) {
        this.iaClient = iaClient;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "api", "ok",
                "ia", iaClient.estaDisponivel() ? "ok" : "offline"));
    }
}
