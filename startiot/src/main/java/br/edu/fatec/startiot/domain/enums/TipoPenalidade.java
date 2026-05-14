package br.edu.fatec.startiot.domain.enums;

public enum TipoPenalidade {
    SIMPLES(20_000L),   // +20 segundos
    GRAVE(120_000L);    // +2 minutos

    private final long valorMs;

    TipoPenalidade(long valorMs) {
        this.valorMs = valorMs;
    }

    public long getValorMs() {
        return valorMs;
    }
}
