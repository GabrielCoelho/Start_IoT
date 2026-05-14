package br.edu.fatec.startiot.dto.request;

import br.edu.fatec.startiot.domain.enums.TipoPenalidade;

public record ValidarTempoRequest(
        TipoPenalidade tipoPenalidade,
        String motivo
) {}
