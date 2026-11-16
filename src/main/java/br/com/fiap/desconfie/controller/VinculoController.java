package br.com.fiap.desconfie.controller;

import br.com.fiap.desconfie.dto.request.DependenteRequest;
import br.com.fiap.desconfie.dto.response.MembroCirculoResponse;
import br.com.fiap.desconfie.dto.response.VinculoResponse;
import br.com.fiap.desconfie.service.VinculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Rotas do círculo de proteção. O {id} da URL é o usuário dono da operação
 * (o guardião, nas rotas de dependentes).
 */
@RestController
@RequestMapping("/usuarios/{id}")
public class VinculoController {

    private final VinculoService vinculoService;

    public VinculoController(VinculoService vinculoService) {
        this.vinculoService = vinculoService;
    }

    /** POST /usuarios/{id}/dependentes -> 201, ou 409 no quarto dependente (RN01). */
    @PostMapping("/dependentes")
    public ResponseEntity<VinculoResponse> adicionarDependente(@PathVariable Long id,
                                                               @Valid @RequestBody DependenteRequest request) {
        VinculoResponse resposta = vinculoService.adicionarDependente(id, request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{dependenteId}").buildAndExpand(resposta.getDependente().getId()).toUri();
        return ResponseEntity.created(uri).body(resposta);
    }

    @GetMapping("/dependentes")
    public ResponseEntity<List<MembroCirculoResponse>> listarDependentes(@PathVariable Long id) {
        return ResponseEntity.ok(vinculoService.listarDependentes(id));
    }

    @DeleteMapping("/dependentes/{dependenteId}")
    public ResponseEntity<Void> removerDependente(@PathVariable Long id, @PathVariable Long dependenteId) {
        vinculoService.removerDependente(id, dependenteId);
        return ResponseEntity.noContent().build();
    }

    /** GET /usuarios/{id}/guardiao -> o guardião deste usuário, ou 404 se não tiver. */
    @GetMapping("/guardiao")
    public ResponseEntity<MembroCirculoResponse> buscarGuardiao(@PathVariable Long id) {
        return ResponseEntity.ok(vinculoService.buscarGuardiao(id));
    }
}
