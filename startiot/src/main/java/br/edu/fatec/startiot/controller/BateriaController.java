package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.BateriaRequest;
import br.edu.fatec.startiot.dto.response.BateriaResponse;
import br.edu.fatec.startiot.service.BateriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/baterias")
@RequiredArgsConstructor
public class BateriaController {

    private final BateriaService bateriaService;

    @PostMapping
    public ResponseEntity<BateriaResponse> criar(@Valid @RequestBody BateriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bateriaService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BateriaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<BateriaResponse>> listarPorEdicao(@RequestParam Long edicaoId) {
        return ResponseEntity.ok(bateriaService.listarPorEdicao(edicaoId));
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<BateriaResponse> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.iniciar(id));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<BateriaResponse> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.finalizar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<BateriaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.cancelar(id));
    }
}
