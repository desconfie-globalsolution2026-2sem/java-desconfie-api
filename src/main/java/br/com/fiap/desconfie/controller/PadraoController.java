package br.com.fiap.desconfie.controller;

import br.com.fiap.desconfie.dto.request.PadraoRequest;
import br.com.fiap.desconfie.dto.response.PadraoResponse;
import br.com.fiap.desconfie.service.PadraoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Rotas dos padrões de golpe. O POST daqui serve principalmente para DOMINIO_OFICIAL,
 * que não tem categoria; os outros tipos também podem ser criados por /categorias/{id}/padroes.
 */
@RestController
@RequestMapping("/padroes")
public class PadraoController {

    private final PadraoService padraoService;

    public PadraoController(PadraoService padraoService) {
        this.padraoService = padraoService;
    }

    @PostMapping
    public ResponseEntity<PadraoResponse> cadastrar(@Valid @RequestBody PadraoRequest request) {
        PadraoResponse resposta = padraoService.cadastrar(request, null);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.getId()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<PadraoResponse>> listar() {
        return ResponseEntity.ok(padraoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PadraoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(padraoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PadraoResponse> atualizar(@PathVariable Long id,
                                                    @Valid @RequestBody PadraoRequest request) {
        return ResponseEntity.ok(padraoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        padraoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
