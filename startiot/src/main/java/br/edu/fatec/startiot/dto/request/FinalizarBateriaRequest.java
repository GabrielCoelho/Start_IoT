package br.edu.fatec.startiot.dto.request;

public record FinalizarBateriaRequest(
        /** Quantas equipes avançam. Null = sem corte (todas avançam). */
        Integer posicaoCorte
) {}
