package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.EquipeRequest;
import br.edu.fatec.startiot.dto.response.EquipeResponse;
import br.edu.fatec.startiot.service.EquipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;

    @PostMapping
    public ResponseEntity<EquipeResponse> inscrever(@Valid @RequestBody EquipeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipeService.inscrever(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EquipeResponse>> listar(
            @RequestParam Long edicaoId,
            @RequestParam(required = false, defaultValue = "false") boolean apenasAprovadas) {
        if (apenasAprovadas) {
            return ResponseEntity.ok(equipeService.listarAprovadas(edicaoId));
        }
        return ResponseEntity.ok(equipeService.listarPorEdicao(edicaoId));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<EquipeResponse> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.aprovar(id));
    }

    @PatchMapping("/{id}/reprovar")
    public ResponseEntity<EquipeResponse> reprovar(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.reprovar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<EquipeResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.cancelar(id));
    }
}
