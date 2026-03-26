package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.MembroRequest;
import br.edu.fatec.startiot.dto.response.MembroResponse;
import br.edu.fatec.startiot.service.MembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Membros", description = "Gerenciamento dos integrantes de cada equipe participante")
@RestController
@RequestMapping("/api/membros")
@RequiredArgsConstructor
public class MembroController {

    private final MembroService membroService;

    @Operation(
        summary = "Adicionar membro",
        description = "Vincula um novo integrante a uma equipe. Cada equipe precisa ter o número mínimo de membros " +
                      "exigido pelo regulamento antes de ter sua inscrição aprovada."
    )
    @PostMapping
    public ResponseEntity<MembroResponse> adicionar(@Valid @RequestBody MembroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membroService.adicionar(request));
    }

    @Operation(
        summary = "Buscar membro por ID",
        description = "Retorna os dados de um membro específico (nome, função na equipe, contato)."
    )
    @GetMapping("/{id}")
    public ResponseEntity<MembroResponse> buscarPorId(
            @Parameter(description = "ID do membro") @PathVariable Long id) {
        return ResponseEntity.ok(membroService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar membros da equipe",
        description = "Retorna todos os integrantes de uma equipe. Use para exibir a composição da equipe na tela de detalhes " +
                      "ou para validar o número de membros antes de aprovar a inscrição."
    )
    @GetMapping
    public ResponseEntity<List<MembroResponse>> listarPorEquipe(
            @Parameter(description = "ID da equipe", required = true) @RequestParam Long equipeId) {
        return ResponseEntity.ok(membroService.listarPorEquipe(equipeId));
    }

    @Operation(
        summary = "Atualizar membro",
        description = "Atualiza os dados de um membro da equipe (nome, função, contato). Use para corrigir informações cadastradas incorretamente."
    )
    @PutMapping("/{id}")
    public ResponseEntity<MembroResponse> atualizar(
            @Parameter(description = "ID do membro") @PathVariable Long id,
            @Valid @RequestBody MembroRequest request) {
        return ResponseEntity.ok(membroService.atualizar(id, request));
    }

    @Operation(
        summary = "Remover membro",
        description = "Remove permanentemente um integrante da equipe. Use quando um aluno abandonar o projeto antes do evento. " +
                      "A remoção pode invalidar a aprovação da equipe se o número mínimo de membros não for mais atendido."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(description = "ID do membro") @PathVariable Long id) {
        membroService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
