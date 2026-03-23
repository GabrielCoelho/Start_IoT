package br.edu.fatec.startiot.dto.request;

import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EdicaoRequest(

        @NotNull(message = "Evento é obrigatório")
        Long eventoId,

        @NotNull(message = "Ano é obrigatório")
        Integer ano,

        LocalDate dataEvento,

        @NotNull(message = "Status é obrigatório")
        StatusEdicao status
) {}
