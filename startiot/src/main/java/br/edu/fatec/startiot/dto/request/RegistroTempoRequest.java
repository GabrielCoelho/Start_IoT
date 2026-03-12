package br.edu.fatec.startiot.dto.request;

import br.edu.fatec.startiot.domain.enums.TipoRegistro;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistroTempoRequest(

        @NotNull(message = "Corrida é obrigatória")
        Long corridaId,

        @NotNull(message = "Equipe é obrigatória")
        Long equipeId,

        @NotNull(message = "Tempo é obrigatório")
        @Positive(message = "Tempo deve ser positivo")
        Double tempoMilissegundos,

        @NotNull(message = "Tipo de registro é obrigatório")
        TipoRegistro tipoRegistro,

        String observacoes
) {}
