package br.edu.fatec.startiot.dto.response;

import java.util.List;

public record RankingItemResponse(
        Integer posicao,
        Long equipeId,
        String equipeNome,
        String equipeCurso,
        Integer totalDescidas,
        Double melhorTempo,
        Double ultimoTempo,
        Double mediaTempo,
        List<BateriaTempoItem> porBateria
) {}
