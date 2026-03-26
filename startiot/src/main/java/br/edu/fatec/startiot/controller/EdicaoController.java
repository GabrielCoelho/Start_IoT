package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.dto.request.EdicaoRequest;
import br.edu.fatec.startiot.dto.response.EdicaoResponse;
import br.edu.fatec.startiot.service.EdicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Edições", description = "Gerenciamento de edições anuais de um evento (ex: Start IoT 2024, Start IoT 2025)")
@RestController
@RequestMapping("/api/edicoes")
@RequiredArgsConstructor
public class EdicaoController {

    private final EdicaoService edicaoService;

    @Operation(
        summary = "Criar edição",
        description = "Cria uma nova edição vinculada a um evento existente. Uma Edição representa a realização anual/semestral do evento " +
                      "e agrupa todas as baterias, inscrições de equipes e rankings daquele período."
    )
    @PostMapping
    public ResponseEntity<EdicaoResponse> criar(@Valid @RequestBody EdicaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(edicaoService.criar(request));
    }

    @Operation(
        summary = "Buscar edição por ID",
        description = "Retorna os dados de uma edição específica, incluindo seu status atual. " +
                      "Use para verificar se a edição está em fase de inscrições, em andamento ou encerrada antes de realizar operações."
    )
    @GetMapping("/{id}")
    public ResponseEntity<EdicaoResponse> buscarPorId(
            @Parameter(description = "ID da edição") @PathVariable Long id) {
        return ResponseEntity.ok(edicaoService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar edições",
        description = "Lista edições filtradas por evento ou por status. " +
                      "Informe 'eventoId' para ver todas as edições de um evento, ou 'status' para filtrar por fase " +
                      "(ex: INSCRICOES_ABERTAS para exibir eventos com inscrições disponíveis). Ao menos um parâmetro é obrigatório."
    )
    @GetMapping
    public ResponseEntity<List<EdicaoResponse>> listar(
            @Parameter(description = "ID do evento pai") @RequestParam(required = false) Long eventoId,
            @Parameter(description = "Status da edição (ex: INSCRICOES_ABERTAS, EM_ANDAMENTO, ENCERRADA)") @RequestParam(required = false) StatusEdicao status) {
        if (eventoId != null) {
            return ResponseEntity.ok(edicaoService.listarPorEvento(eventoId));
        }
        if (status != null) {
            return ResponseEntity.ok(edicaoService.listarPorStatus(status));
        }
        return ResponseEntity.badRequest().build();
    }

    @Operation(
        summary = "Atualizar status da edição",
        description = "Avança ou reverte o status do ciclo de vida de uma edição. " +
                      "Use para abrir inscrições, iniciar o evento, encerrá-lo ou cancelá-lo. " +
                      "O status controla quais operações (inscrição de equipes, criação de baterias, etc.) estão permitidas."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<EdicaoResponse> atualizarStatus(
            @Parameter(description = "ID da edição") @PathVariable Long id,
            @Parameter(description = "Novo status da edição") @RequestParam StatusEdicao novoStatus) {
        return ResponseEntity.ok(edicaoService.atualizarStatus(id, novoStatus));
    }
}
