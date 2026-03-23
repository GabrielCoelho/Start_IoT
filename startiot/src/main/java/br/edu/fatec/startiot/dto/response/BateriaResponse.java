package br.edu.fatec.startiot.dto.response;

import br.edu.fatec.startiot.domain.enums.StatusBateria;

import java.time.LocalDateTime;

public record BateriaResponse(
        Long id,
        Long edicaoId,
        Integer numero,
        String tipo,
        LocalDateTime horarioPrevisto,
        StatusBateria status,
        Integer totalCorridas,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
