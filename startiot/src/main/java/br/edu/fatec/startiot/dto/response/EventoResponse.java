package br.edu.fatec.startiot.dto.response;

import java.time.LocalDateTime;

public record EventoResponse(
        Long id,
        String nome,
        String descricao,
        Integer totalEdicoes,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
