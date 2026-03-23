package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.MembroRequest;
import br.edu.fatec.startiot.dto.response.MembroResponse;
import br.edu.fatec.startiot.service.MembroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membros")
@RequiredArgsConstructor
public class MembroController {

    private final MembroService membroService;

    @PostMapping
    public ResponseEntity<MembroResponse> adicionar(@Valid @RequestBody MembroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membroService.adicionar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembroResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(membroService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<MembroResponse>> listarPorEquipe(@RequestParam Long equipeId) {
        return ResponseEntity.ok(membroService.listarPorEquipe(equipeId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembroResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MembroRequest request) {
        return ResponseEntity.ok(membroService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        membroService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
