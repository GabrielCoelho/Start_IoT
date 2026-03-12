package br.edu.fatec.startiot.dto.request;

import jakarta.validation.constraints.NotNull;

public record CorridaRequest(

        @NotNull(message = "Bateria é obrigatória")
        Long bateriaId,

        @NotNull(message = "Ordem é obrigatória")
        Integer ordem
) {}
