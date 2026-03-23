package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.dto.request.EdicaoRequest;
import br.edu.fatec.startiot.dto.response.EdicaoResponse;
import br.edu.fatec.startiot.service.EdicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edicoes")
@RequiredArgsConstructor
public class EdicaoController {

    private final EdicaoService edicaoService;

    @PostMapping
    public ResponseEntity<EdicaoResponse> criar(@Valid @RequestBody EdicaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(edicaoService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EdicaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(edicaoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EdicaoResponse>> listar(
            @RequestParam(required = false) Long eventoId,
            @RequestParam(required = false) StatusEdicao status) {
        if (eventoId != null) {
            return ResponseEntity.ok(edicaoService.listarPorEvento(eventoId));
        }
        if (status != null) {
            return ResponseEntity.ok(edicaoService.listarPorStatus(status));
        }
        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EdicaoResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusEdicao novoStatus) {
        return ResponseEntity.ok(edicaoService.atualizarStatus(id, novoStatus));
    }
}
