package br.edu.fatec.startiot.dto.response;

import java.time.LocalDateTime;

public record MembroResponse(
        Long id,
        Long equipeId,
        String equipeNome,
        String nome,
        String ra,
        String funcao,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
