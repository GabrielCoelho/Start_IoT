package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.response.RankingResponse;
import br.edu.fatec.startiot.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ranking", description = "Consulta de classificação geral de uma edição do evento")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(
        summary = "Calcular ranking da edição",
        description = "Calcula e retorna a classificação geral de todas as equipes de uma edição, ordenadas pelo melhor tempo " +
                      "acumulado entre as corridas válidas. Pode ser consultado a qualquer momento durante o evento para acompanhar " +
                      "a classificação parcial, ou ao final para divulgar o resultado oficial."
    )
    @GetMapping("/{edicaoId}")
    public ResponseEntity<RankingResponse> calcularRanking(
            @Parameter(description = "ID da edição para calcular o ranking") @PathVariable Long edicaoId) {
        return ResponseEntity.ok(rankingService.calcularRanking(edicaoId));
    }
}
