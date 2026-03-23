package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.response.RankingResponse;
import br.edu.fatec.startiot.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/{edicaoId}")
    public ResponseEntity<RankingResponse> calcularRanking(@PathVariable Long edicaoId) {
        return ResponseEntity.ok(rankingService.calcularRanking(edicaoId));
    }
}
