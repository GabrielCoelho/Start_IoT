package br.edu.fatec.startiot.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BateriaRequest(

        @NotNull(message = "Edição é obrigatória")
        Long edicaoId,

        @NotNull(message = "Número é obrigatório")
        Integer numero,

        @Size(max = 50, message = "Tipo deve ter no máximo 50 caracteres")
        String tipo,

        LocalDateTime horarioPrevisto
) {}
