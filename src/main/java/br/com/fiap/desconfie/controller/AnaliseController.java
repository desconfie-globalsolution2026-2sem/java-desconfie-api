package br.com.fiap.desconfie.controller;

import br.com.fiap.desconfie.dto.request.AnaliseRequest;
import br.com.fiap.desconfie.dto.request.FeedbackRequest;
import br.com.fiap.desconfie.dto.response.AnaliseResponse;
import br.com.fiap.desconfie.dto.response.AnaliseResumoResponse;
import br.com.fiap.desconfie.dto.response.CirculoAnaliseResponse;
import br.com.fiap.desconfie.service.AnaliseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Rotas de análise. A análise é criada pela rota aninhada do 1:N
 * (/usuarios/{id}/analises), mas "mora" em /analises/{id}.
 */
@RestController
public class AnaliseController {

    private final AnaliseService analiseService;

    public AnaliseController(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    /** POST /usuarios/{id}/analises -> 201 + Location: /analises/{idAnalise} */
    @PostMapping("/usuarios/{id}/analises")
    public ResponseEntity<AnaliseResponse> analisar(@PathVariable Long id, @Valid @RequestBody AnaliseRequest request) {
        AnaliseResponse resposta = analiseService.analisar(id, request);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/analises/{id}").buildAndExpand(resposta.getId()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    /** Histórico do usuário, da mais recente para a mais antiga. */
    @GetMapping("/usuarios/{id}/analises")
    public ResponseEntity<List<AnaliseResumoResponse>> listarPorUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(analiseService.listarPorUsuario(id));
    }

    /** Histórico compartilhado do círculo (só amarelo e vermelho). */
    @GetMapping("/usuarios/{id}/circulo/analises")
    public ResponseEntity<List<CirculoAnaliseResponse>> listarDoCirculo(@PathVariable Long id) {
        return ResponseEntity.ok(analiseService.listarDoCirculo(id));
    }

    @GetMapping("/analises")
    public ResponseEntity<List<AnaliseResumoResponse>> listar() {
        return ResponseEntity.ok(analiseService.listar());
    }

    @GetMapping("/analises/{id}")
    public ResponseEntity<AnaliseResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(analiseService.buscarPorId(id));
    }

    /** PUT /analises/{id}: grava o feedback (ERA_GOLPE ou FALSO_ALARME). */
    @PutMapping("/analises/{id}")
    public ResponseEntity<AnaliseResponse> atualizarFeedback(@PathVariable Long id,
                                                             @Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(analiseService.atualizarFeedback(id, request));
    }

    @DeleteMapping("/analises/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        analiseService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
