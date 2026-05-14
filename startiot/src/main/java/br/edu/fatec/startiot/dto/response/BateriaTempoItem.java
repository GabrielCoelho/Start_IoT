package br.edu.fatec.startiot.dto.response;

public record BateriaTempoItem(
        Long bateriaId,
        Integer bateriaNumero,
        String bateriaTipo,
        Double melhorTempo,
        Integer totalDescidas
) {}
