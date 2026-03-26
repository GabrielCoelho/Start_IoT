package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.RegistroTempoRequest;
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

    // TODO: substituir X-Usuario-Id por extração do JWT na Fase de Segurança
    @Operation(
        summary = "Registrar tempo",
        description = "Submete o tempo cronometrado de uma equipe em uma corrida. Deve ser chamado pelo árbitro assim que a equipe " +
                      "cruzar a linha de chegada. O header 'X-Usuario-Id' identifica o árbitro responsável pelo registro " +
                      "(será substituído por JWT na fase de segurança). A corrida precisa estar com status EM_ANDAMENTO."
    )
    @PostMapping
    public ResponseEntity<RegistroTempoResponse> registrar(
            @Valid @RequestBody RegistroTempoRequest request,
            @Parameter(description = "ID do árbitro que está registrando o tempo (temporário, será substituído por JWT)") @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroTempoService.registrar(request, usuarioId));
    }

    @Operation(
        summary = "Buscar registro por ID",
        description = "Retorna os detalhes de um registro de tempo específico, incluindo o árbitro responsável e o status de validação."
    )
    @GetMapping("/{id}")
    public ResponseEntity<RegistroTempoResponse> buscarPorId(
            @Parameter(description = "ID do registro de tempo") @PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar registros da corrida",
        description = "Retorna todos os tempos registrados em uma corrida. Use para exibir o placar parcial durante a corrida " +
                      "ou para o juiz revisar e validar os tempos ao final."
    )
    @GetMapping
    public ResponseEntity<List<RegistroTempoResponse>> listarPorCorrida(
            @Parameter(description = "ID da corrida", required = true) @RequestParam Long corridaId) {
        return ResponseEntity.ok(registroTempoService.listarPorCorrida(corridaId));
    }

    @Operation(
        summary = "Validar registro de tempo",
        description = "O juiz confirma que o tempo registrado é válido e deve ser considerado no ranking. " +
                      "Apenas registros validados entram no cálculo de classificação."
    )
    @PatchMapping("/{id}/validar")
    public ResponseEntity<RegistroTempoResponse> validar(
            @Parameter(description = "ID do registro de tempo") @PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.validar(id));
    }

    @Operation(
        summary = "Invalidar registro de tempo",
        description = "O juiz anula um tempo registrado por erro do árbitro, falha do sensor ou infração da equipe. " +
                      "Registros invalidados são excluídos do ranking mas mantidos no histórico para auditoria."
    )
    @PatchMapping("/{id}/invalidar")
    public ResponseEntity<RegistroTempoResponse> invalidar(
            @Parameter(description = "ID do registro de tempo") @PathVariable Long id) {
        return ResponseEntity.ok(registroTempoService.invalidar(id));
    }
}
