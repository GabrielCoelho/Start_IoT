package br.edu.fatec.startiot.dto.response;

import br.edu.fatec.startiot.domain.enums.StatusCorrida;

import java.time.LocalDateTime;

public record CorridaResponse(
        Long id,
        Long bateriaId,
        Integer bateriaNumero,
        Integer ordem,
        LocalDateTime horarioInicio,
        LocalDateTime horarioFim,
        StatusCorrida status,
        Integer totalRegistros,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
