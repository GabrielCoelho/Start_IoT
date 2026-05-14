package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.BateriaRequest;
import br.edu.fatec.startiot.dto.request.FinalizarBateriaRequest;
import br.edu.fatec.startiot.dto.response.BateriaResponse;
import br.edu.fatec.startiot.dto.response.EquipeResponse;
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

    @Operation(summary = "Criar bateria")
    @PostMapping
    public ResponseEntity<BateriaResponse> criar(@Valid @RequestBody BateriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bateriaService.criar(request));
    }

    @Operation(summary = "Buscar bateria por ID")
    @GetMapping("/{id}")
    public ResponseEntity<BateriaResponse> buscarPorId(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.buscarPorId(id));
    }

    @Operation(summary = "Listar baterias da edição")
    @GetMapping
    public ResponseEntity<List<BateriaResponse>> listarPorEdicao(
            @Parameter(description = "ID da edição", required = true) @RequestParam Long edicaoId) {
        return ResponseEntity.ok(bateriaService.listarPorEdicao(edicaoId));
    }

    @Operation(summary = "Equipes disponíveis para a próxima bateria",
        description = "Retorna equipes aprovadas da edição que não foram eliminadas em baterias anteriores.")
    @GetMapping("/{id}/equipes-disponiveis")
    public ResponseEntity<List<EquipeResponse>> listarEquipesDisponiveis(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.listarEquipesDisponiveis(id));
    }

    @Operation(summary = "Iniciar bateria")
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<BateriaResponse> iniciar(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.iniciar(id));
    }

    @Operation(summary = "Finalizar bateria",
        description = "Encerra a bateria. Se posicaoCorte for informado, equipes abaixo dessa posição " +
                      "são eliminadas e não aparecerão para alocação nas baterias seguintes.")
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<BateriaResponse> finalizar(
            @Parameter(description = "ID da bateria") @PathVariable Long id,
            @RequestBody(required = false) FinalizarBateriaRequest request) {
        return ResponseEntity.ok(bateriaService.finalizar(id, request));
    }

    @Operation(summary = "Cancelar bateria")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<BateriaResponse> cancelar(
            @Parameter(description = "ID da bateria") @PathVariable Long id) {
        return ResponseEntity.ok(bateriaService.cancelar(id));
    }
}
