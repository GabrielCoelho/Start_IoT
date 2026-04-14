package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.AlocacaoEquipeCorridaRequest;
import br.edu.fatec.startiot.dto.request.CorridaRequest;
import br.edu.fatec.startiot.dto.response.AlocacaoEquipeCorridaResponse;
import br.edu.fatec.startiot.dto.response.CorridaResponse;
import br.edu.fatec.startiot.service.CorridaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Corridas", description = "Gerenciamento das corridas individuais dentro de uma bateria")
@RestController
@RequestMapping("/api/corridas")
@RequiredArgsConstructor
public class CorridaController {

    private final CorridaService corridaService;

    @Operation(
        summary = "Criar corrida",
        description = "Cria uma corrida dentro de uma bateria, definindo as equipes participantes e a ordem de largada. " +
                      "Uma Corrida é a unidade mínima da competição onde os tempos são registrados pelos árbitros."
    )
    @PostMapping
    public ResponseEntity<CorridaResponse> criar(@Valid @RequestBody CorridaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridaService.criar(request));
    }

    @Operation(
        summary = "Buscar corrida por ID",
        description = "Retorna os dados de uma corrida específica, incluindo equipes participantes, status e registros de tempo vinculados."
    )
    @GetMapping("/{id}")
    public ResponseEntity<CorridaResponse> buscarPorId(
            @Parameter(description = "ID da corrida") @PathVariable Long id) {
        return ResponseEntity.ok(corridaService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar corridas da bateria",
        description = "Retorna todas as corridas de uma bateria em ordem de criação. " +
                      "Use para exibir a grade de corridas no painel do árbitro durante o evento."
    )
    @GetMapping
    public ResponseEntity<List<CorridaResponse>> listarPorBateria(
            @Parameter(description = "ID da bateria", required = true) @RequestParam Long bateriaId) {
        return ResponseEntity.ok(corridaService.listarPorBateria(bateriaId));
    }

    @Operation(
        summary = "Iniciar corrida",
        description = "Marca a corrida como em andamento e registra o horário de início. " +
                      "A partir deste momento, os árbitros podem registrar os tempos das equipes participantes via /api/registros-tempo."
    )
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<CorridaResponse> iniciar(
            @Parameter(description = "ID da corrida") @PathVariable Long id) {
        return ResponseEntity.ok(corridaService.iniciar(id));
    }

    @Operation(
        summary = "Finalizar corrida",
        description = "Encerra a corrida e bloqueia novos registros de tempo. Os tempos já registrados e validados serão considerados no ranking. " +
                      "Acione após a última equipe cruzar a linha de chegada."
    )
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<CorridaResponse> finalizar(
            @Parameter(description = "ID da corrida") @PathVariable Long id) {
        return ResponseEntity.ok(corridaService.finalizar(id));
    }

    @Operation(
        summary = "Cancelar corrida",
        description = "Cancela uma corrida que ainda não foi finalizada. Os registros de tempo coletados são desconsiderados. " +
                      "Use em caso de acidente, falha técnica generalizada ou qualquer incidente que impeça a conclusão regular da corrida."
    )
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CorridaResponse> cancelar(
            @Parameter(description = "ID da corrida") @PathVariable Long id) {
        return ResponseEntity.ok(corridaService.cancelar(id));
    }

    @Operation(
        summary = "Desclassificar corrida",
        description = "Desclassifica uma equipe de uma corrida já finalizada por infração ao regulamento (colisão intencional, " +
                      "uso de peças não homologadas, etc.). Os tempos da equipe desclassificada são removidos do ranking."
    )
    @PatchMapping("/{id}/desclassificar")
    public ResponseEntity<CorridaResponse> desclassificar(
            @Parameter(description = "ID da corrida") @PathVariable Long id) {
        return ResponseEntity.ok(corridaService.desclassificar(id));
    }

    // -------------------------------------------------------------------------
    // Pré-alocação de equipes
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Alocar equipe na corrida",
        description = "Pré-aloca uma equipe em uma corrida antes do seu início. " +
                      "Ao existir ao menos uma alocação, somente as equipes alocadas poderão ter tempos " +
                      "registrados nesta corrida via /api/registros-tempo, evitando erros de vinculação " +
                      "durante a cronometragem. A alocação só é permitida enquanto a corrida estiver com " +
                      "status AGUARDANDO. A equipe deve estar APROVADA e pertencer à mesma edição."
    )
    @PostMapping("/{id}/equipes")
    public ResponseEntity<AlocacaoEquipeCorridaResponse> alocarEquipe(
            @Parameter(description = "ID da corrida") @PathVariable Long id,
            @Valid @RequestBody AlocacaoEquipeCorridaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridaService.alocarEquipe(id, request));
    }

    @Operation(
        summary = "Remover alocação de equipe",
        description = "Remove a pré-alocação de uma equipe da corrida. " +
                      "Só é possível remover enquanto a corrida estiver com status AGUARDANDO. " +
                      "Retorna 204 No Content em caso de sucesso."
    )
    @DeleteMapping("/{id}/equipes/{equipeId}")
    public ResponseEntity<Void> removerAlocacao(
            @Parameter(description = "ID da corrida") @PathVariable Long id,
            @Parameter(description = "ID da equipe a ser removida da corrida") @PathVariable Long equipeId) {
        corridaService.removerAlocacao(id, equipeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Listar equipes alocadas na corrida",
        description = "Retorna todas as equipes pré-alocadas em uma corrida, ordenadas por data de alocação. " +
                      "Use este endpoint no frontend de cronometragem para exibir apenas as equipes " +
                      "participantes, sem precisar carregar a lista completa de equipes da edição."
    )
    @GetMapping("/{id}/equipes")
    public ResponseEntity<List<AlocacaoEquipeCorridaResponse>> listarAlocacoes(
            @Parameter(description = "ID da corrida") @PathVariable Long id) {
        return ResponseEntity.ok(corridaService.listarAlocacoes(id));
    }
}
