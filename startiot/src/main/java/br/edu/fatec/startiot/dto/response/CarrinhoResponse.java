package br.edu.fatec.startiot.dto.response;

import java.time.LocalDateTime;

public record CarrinhoResponse(
        Long id,
        Long equipeId,
        String equipeNome,
        String identificacao,
        Boolean aprovadoVistoria,
        String observacoesVistoria,
        LocalDateTime dataVistoria,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
