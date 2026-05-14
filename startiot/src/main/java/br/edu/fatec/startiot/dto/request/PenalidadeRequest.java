package br.edu.fatec.startiot.dto.request;

import br.edu.fatec.startiot.domain.enums.TipoPenalidade;
import jakarta.validation.constraints.NotNull;

public record PenalidadeRequest(
        @NotNull(message = "Tipo de penalidade é obrigatório")
        TipoPenalidade tipo,
        String motivo
) {}
