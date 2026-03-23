package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.RegistroTempoRequest;
import br.edu.fatec.startiot.dto.response.RegistroTempoResponse;
import br.edu.fatec.startiot.service.RegistroTempoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registros-tempo")
@RequiredArgsConstructor
public class RegistroTempoController {

    private final RegistroTempoService registroTempoService;

    // TODO: substituir X-Usuario-Id por extração do JWT na Fase de Segurança
    @PostMapping
    public ResponseEntity<RegistroTempoResponse> registrar(
            @Valid @RequestBody RegistroTempoRequest request,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroTempoService.registrar(request, usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroTempoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<RegistroTempoResponse>> listarPorCorrida(@RequestParam Long corridaId) {
        return ResponseEntity.ok(registroTempoService.listarPorCorrida(corridaId));
    }

    @PatchMapping("/{id}/validar")
    public ResponseEntity<RegistroTempoResponse> validar(@PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.validar(id));
    }

    @PatchMapping("/{id}/invalidar")
    public ResponseEntity<RegistroTempoResponse> invalidar(@PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.invalidar(id));
    }
}
