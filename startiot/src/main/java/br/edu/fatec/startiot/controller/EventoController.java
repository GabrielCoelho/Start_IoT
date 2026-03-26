package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.EventoRequest;
import br.edu.fatec.startiot.dto.response.EventoResponse;
import br.edu.fatec.startiot.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Eventos", description = "Gerenciamento de eventos do campeonato Start IoT (entidade raiz da hierarquia)")
@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @Operation(
        summary = "Criar evento",
        description = "Cria um novo evento do campeonato. Um Evento é a entidade de mais alto nível da hierarquia (Evento → Edição → Bateria → Corrida). " +
                      "Deve ser criado pelo organizador antes de qualquer edição poder ser cadastrada."
    )
    @PostMapping
    public ResponseEntity<EventoResponse> criar(@Valid @RequestBody EventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.criar(request));
    }

    @Operation(
        summary = "Buscar evento por ID",
        description = "Retorna os dados de um evento específico. Use para carregar as informações de um evento antes de gerenciar suas edições."
    )
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponse> buscarPorId(
            @Parameter(description = "ID do evento") @PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar todos os eventos",
        description = "Retorna todos os eventos cadastrados no sistema. Utilizado na tela inicial para exibir o histórico de campeonatos e permitir navegação para suas edições."
    )
    @GetMapping
    public ResponseEntity<List<EventoResponse>> listar() {
        return ResponseEntity.ok(eventoService.listar());
    }

    @Operation(
        summary = "Atualizar evento",
        description = "Atualiza os dados cadastrais de um evento existente (nome, descrição, local, etc.). " +
                      "Use para corrigir informações antes da abertura das inscrições."
    )
    @PutMapping("/{id}")
    public ResponseEntity<EventoResponse> atualizar(
            @Parameter(description = "ID do evento") @PathVariable Long id,
            @Valid @RequestBody EventoRequest request) {
        return ResponseEntity.ok(eventoService.atualizar(id, request));
    }
}
