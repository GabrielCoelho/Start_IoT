package br.edu.fatec.startiot.dto.response;

import br.edu.fatec.startiot.domain.enums.TipoPenalidade;
import br.edu.fatec.startiot.domain.enums.TipoRegistro;

import java.time.LocalDateTime;

public record RegistroTempoResponse(
        Long id,
        Long corridaId,
        Long equipeId,
        String equipeNome,
        Long usuarioId,
        String usuarioNome,
        LocalDateTime timestampRegistro,
        Double tempoMilissegundos,
        TipoRegistro tipoRegistro,
        Boolean validado,
        String observacoes,
        TipoPenalidade tipoPenalidade,
        String motivoPenalidade,
        Boolean penalideVistoria,
        Double tempoEfetivo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
