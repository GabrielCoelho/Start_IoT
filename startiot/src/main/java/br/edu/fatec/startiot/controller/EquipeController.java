package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.EquipeRequest;
import br.edu.fatec.startiot.dto.response.EquipeResponse;
import br.edu.fatec.startiot.service.EquipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Equipes", description = "Inscrição e gerenciamento de equipes participantes de uma edição")
@RestController
@RequestMapping("/api/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;

    @Operation(
        summary = "Inscrever equipe",
        description = "Registra a inscrição de uma equipe em uma edição do evento. A equipe é criada com status PENDENTE e aguarda " +
                      "aprovação do organizador. Só é permitido enquanto a edição estiver com inscrições abertas."
    )
    @PostMapping
    public ResponseEntity<EquipeResponse> inscrever(@Valid @RequestBody EquipeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipeService.inscrever(request));
    }

    @Operation(
        summary = "Buscar equipe por ID",
        description = "Retorna os dados completos de uma equipe, incluindo status de inscrição e membros vinculados."
    )
    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponse> buscarPorId(
            @Parameter(description = "ID da equipe") @PathVariable Long id) {
        return ResponseEntity.ok(equipeService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar equipes da edição",
        description = "Lista todas as equipes inscritas em uma edição. Use 'apenasAprovadas=true' para retornar somente equipes " +
                      "habilitadas a participar das corridas (útil ao montar baterias)."
    )
    @GetMapping
    public ResponseEntity<List<EquipeResponse>> listar(
            @Parameter(description = "ID da edição", required = true) @RequestParam Long edicaoId,
            @Parameter(description = "Se true, retorna apenas equipes com inscrição aprovada") @RequestParam(required = false, defaultValue = "false") boolean apenasAprovadas) {
        if (apenasAprovadas) {
            return ResponseEntity.ok(equipeService.listarAprovadas(edicaoId));
        }
        return ResponseEntity.ok(equipeService.listarPorEdicao(edicaoId));
    }

    @Operation(
        summary = "Aprovar inscrição",
        description = "Aprova a inscrição de uma equipe, habilitando-a a participar das corridas da edição. " +
                      "Realizado pelo organizador após verificar que a equipe atende aos critérios de participação."
    )
    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<EquipeResponse> aprovar(
            @Parameter(description = "ID da equipe") @PathVariable Long id) {
        return ResponseEntity.ok(equipeService.aprovar(id));
    }

    @Operation(
        summary = "Reprovar inscrição",
        description = "Reprova a inscrição de uma equipe, impedindo sua participação na edição. " +
                      "Use quando a equipe não atende aos requisitos mínimos (documentação incompleta, número insuficiente de membros, etc.)."
    )
    @PatchMapping("/{id}/reprovar")
    public ResponseEntity<EquipeResponse> reprovar(
            @Parameter(description = "ID da equipe") @PathVariable Long id) {
        return ResponseEntity.ok(equipeService.reprovar(id));
    }

    @Operation(
        summary = "Cancelar inscrição",
        description = "Cancela a inscrição de uma equipe a pedido da própria equipe ou por decisão administrativa. " +
                      "A equipe cancelada não pode ser reativada — é necessário uma nova inscrição."
    )
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<EquipeResponse> cancelar(
            @Parameter(description = "ID da equipe") @PathVariable Long id) {
        return ResponseEntity.ok(equipeService.cancelar(id));
    }
}
