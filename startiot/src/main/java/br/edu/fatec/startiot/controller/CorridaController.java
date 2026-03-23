package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.CorridaRequest;
import br.edu.fatec.startiot.dto.response.CorridaResponse;
import br.edu.fatec.startiot.service.CorridaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/corridas")
@RequiredArgsConstructor
public class CorridaController {

    private final CorridaService corridaService;

    @PostMapping
    public ResponseEntity<CorridaResponse> criar(@Valid @RequestBody CorridaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridaService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorridaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(corridaService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<CorridaResponse>> listarPorBateria(@RequestParam Long bateriaId) {
        return ResponseEntity.ok(corridaService.listarPorBateria(bateriaId));
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<CorridaResponse> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(corridaService.iniciar(id));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<CorridaResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(corridaService.finalizar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CorridaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(corridaService.cancelar(id));
    }

    @PatchMapping("/{id}/desclassificar")
    public ResponseEntity<CorridaResponse> desclassificar(@PathVariable Long id) {
        return ResponseEntity.ok(corridaService.desclassificar(id));
    }
}
