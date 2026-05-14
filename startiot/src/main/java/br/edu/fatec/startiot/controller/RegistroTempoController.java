package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.PenalidadeRequest;
import br.edu.fatec.startiot.dto.request.RegistroTempoRequest;
import br.edu.fatec.startiot.dto.request.ValidarTempoRequest;
import br.edu.fatec.startiot.dto.response.RegistroTempoResponse;
import br.edu.fatec.startiot.service.RegistroTempoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Registros de Tempo", description = "Captura e validação dos tempos cronometrados pelos árbitros durante as corridas")
@RestController
@RequestMapping("/api/registros-tempo")
@RequiredArgsConstructor
public class RegistroTempoController {

    private final RegistroTempoService registroTempoService;

    @Operation(summary = "Registrar tempo",
        description = "Submete o tempo cronometrado de uma equipe em uma corrida. A corrida precisa estar EM_ANDAMENTO.")
    @PostMapping
    public ResponseEntity<RegistroTempoResponse> registrar(
            @Valid @RequestBody RegistroTempoRequest request,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroTempoService.registrar(request, usuarioId));
    }

    @Operation(summary = "Buscar registro por ID")
    @GetMapping("/{id}")
    public ResponseEntity<RegistroTempoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.buscarPorId(id));
    }

    @Operation(summary = "Listar registros",
        description = "Informe corridaId para listar por corrida, ou edicaoId para listar todos os tempos de chegada de uma edição.")
    @GetMapping
    public ResponseEntity<List<RegistroTempoResponse>> listar(
            @Parameter(description = "ID da corrida") @RequestParam(required = false) Long corridaId,
            @Parameter(description = "ID da edição") @RequestParam(required = false) Long edicaoId) {
        if (corridaId != null) {
            return ResponseEntity.ok(registroTempoService.listarPorCorrida(corridaId));
        }
        if (edicaoId != null) {
            return ResponseEntity.ok(registroTempoService.listarPorEdicao(edicaoId));
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Listar tempos pendentes de validação",
        description = "Retorna os tempos de chegada ainda não validados de uma edição.")
    @GetMapping("/pendentes")
    public ResponseEntity<List<RegistroTempoResponse>> listarPendentes(
            @Parameter(description = "ID da edição") @RequestParam Long edicaoId) {
        return ResponseEntity.ok(registroTempoService.listarPendentesPorEdicao(edicaoId));
    }

    @Operation(summary = "Validar registro de tempo",
        description = "Valida o tempo, marcando-o para o ranking. Opcionalmente aplica penalidade SIMPLES (+20s) ou GRAVE (+2min).")
    @PatchMapping("/{id}/validar")
    public ResponseEntity<RegistroTempoResponse> validar(
            @PathVariable Long id,
            @RequestBody(required = false) ValidarTempoRequest request) {
        return ResponseEntity.ok(registroTempoService.validar(id, request));
    }

    @Operation(summary = "Invalidar registro de tempo")
    @PatchMapping("/{id}/invalidar")
    public ResponseEntity<RegistroTempoResponse> invalidar(@PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.invalidar(id));
    }

    @Operation(summary = "Aplicar penalidade",
        description = "Aplica uma penalidade SIMPLES (+20s) ou GRAVE (+2min) ao tempo da equipe. " +
                      "O tempo efetivo = tempo original + penalidade e é usado no ranking.")
    @PatchMapping("/{id}/penalidade")
    public ResponseEntity<RegistroTempoResponse> aplicarPenalidade(
            @PathVariable Long id,
            @Valid @RequestBody PenalidadeRequest request) {
        return ResponseEntity.ok(registroTempoService.aplicarPenalidade(id, request));
    }

    @Operation(summary = "Remover penalidade",
        description = "Remove a penalidade aplicada ao registro, restaurando o tempo original.")
    @DeleteMapping("/{id}/penalidade")
    public ResponseEntity<RegistroTempoResponse> removerPenalidade(@PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.removerPenalidade(id));
    }
}
