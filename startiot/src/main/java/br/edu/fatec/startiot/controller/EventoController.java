package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.EventoRequest;
import br.edu.fatec.startiot.dto.response.EventoResponse;
import br.edu.fatec.startiot.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    public ResponseEntity<EventoResponse> criar(@Valid @RequestBody EventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EventoResponse>> listar() {
        return ResponseEntity.ok(eventoService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequest request) {
        return ResponseEntity.ok(eventoService.atualizar(id, request));
    }
}
