package br.edu.fatec.startiot.dto.response;

import br.edu.fatec.startiot.domain.enums.StatusEdicao;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EdicaoResponse(
        Long id,
        Long eventoId,
        String eventoNome,
        Integer ano,
        LocalDate dataEvento,
        StatusEdicao status,
        Integer totalEquipes,
        Integer totalBaterias,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
