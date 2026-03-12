package br.edu.fatec.startiot.dto.response;

public record RankingItemResponse(
        Integer posicao,
        Long equipeId,
        String equipeNome,
        String equipeCurso,
        Integer totalDescidas,
        Double melhorTempo,
        Double mediaTempo
) {}
