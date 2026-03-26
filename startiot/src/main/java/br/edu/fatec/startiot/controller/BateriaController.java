package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.BateriaRequest;
import br.edu.fatec.startiot.dto.response.BateriaResponse;
import br.edu.fatec.startiot.service.BateriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Baterias", description = "Gerenciamento de baterias (grupos de corridas) dentro de uma edição")
@RestController
@RequestMapping("/api/baterias")
@RequiredArgsConstructor
public class BateriaController {

    private final BateriaService bateriaService;

    @Operation(
        summary = "Criar bateria",
        description = "Cria uma nova bateria dentro de uma edição. Uma Bateria agrupa múltiplas corridas e representa uma fase da competição " +
                      "(ex: eliminatórias, semifinal, final). Deve ser criada pelo organizador após a edição ser iniciada."
    )
    @PostMapping
    public ResponseEntity<BateriaResponse> criar(@Valid @RequestBody BateriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bateriaService.criar(request));
    }

    @Operation(
        summary = "Buscar bateria por ID",
        description = "Retorna os dados de uma bateria específica e seu status atual. " +
                      "Use para verificar se a bateria está aberta para receber novas corridas."
    )
    @GetMapping("/{id}")
    public ResponseEntity<BateriaResponse> buscarPorId(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar baterias da edição",
        description = "Retorna todas as baterias de uma edição em ordem. Use para montar o painel de acompanhamento da competição, " +
                      "exibindo o progresso de cada fase do evento."
    )
    @GetMapping
    public ResponseEntity<List<BateriaResponse>> listarPorEdicao(
            @Parameter(description = "ID da edição", required = true) @RequestParam Long edicaoId) {
        return ResponseEntity.ok(bateriaService.listarPorEdicao(edicaoId));
    }

    @Operation(
        summary = "Iniciar bateria",
        description = "Marca a bateria como em andamento. A partir deste momento, as corridas da bateria podem ser iniciadas. " +
                      "Acione este endpoint quando a fase correspondente da competição começar no dia do evento."
    )
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<BateriaResponse> iniciar(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.iniciar(id));
    }

    @Operation(
        summary = "Finalizar bateria",
        description = "Encerra a bateria após todas as corridas serem concluídas. Nenhuma nova corrida pode ser adicionada após este ponto. " +
                      "O sistema pode calcular os classificados para a próxima fase."
    )
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<BateriaResponse> finalizar(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.finalizar(id));
    }

    @Operation(
        summary = "Cancelar bateria",
        description = "Cancela uma bateria por motivo de força maior (problema técnico, condições climáticas, etc.). " +
                      "As corridas vinculadas também são desconsideradas para o ranking."
    )
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<BateriaResponse> cancelar(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.cancelar(id));
    }
}
