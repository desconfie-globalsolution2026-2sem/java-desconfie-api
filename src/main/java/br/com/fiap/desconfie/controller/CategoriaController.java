package br.com.fiap.desconfie.controller;

import br.com.fiap.desconfie.dto.request.CategoriaRequest;
import br.com.fiap.desconfie.dto.request.PadraoRequest;
import br.com.fiap.desconfie.dto.response.CategoriaResponse;
import br.com.fiap.desconfie.dto.response.PadraoResponse;
import br.com.fiap.desconfie.service.CategoriaService;
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
 * Rotas das categorias de golpe, incluindo a rota aninhada do 1:N
 * categoria -> padrões (/categorias/{id}/padroes).
 */
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final PadraoService padraoService;

    public CategoriaController(CategoriaService categoriaService, PadraoService padraoService) {
        this.categoriaService = categoriaService;
        this.padraoService = padraoService;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse resposta = categoriaService.cadastrar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resposta.getId()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /categorias/{id}/padroes: os padrões desta categoria. */
    @GetMapping("/{id}/padroes")
    public ResponseEntity<List<PadraoResponse>> listarPadroes(@PathVariable Long id) {
        return ResponseEntity.ok(padraoService.listarPorCategoria(id));
    }

    /** POST /categorias/{id}/padroes: cria um padrão já ligado a esta categoria. */
    @PostMapping("/{id}/padroes")
    public ResponseEntity<PadraoResponse> cadastrarPadrao(@PathVariable Long id,
                                                          @Valid @RequestBody PadraoRequest request) {
        PadraoResponse resposta = padraoService.cadastrar(request, id);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/padroes/{id}").buildAndExpand(resposta.getId()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }
}
