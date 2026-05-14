package br.edu.fatec.startiot.domain.enums;

public enum TipoPenalidade {
    SIMPLES(10_000L),   // +10 segundos
    GRAVE(120_000L);    // +2 minutos

    private final long valorMs;

    TipoPenalidade(long valorMs) {
        this.valorMs = valorMs;
    }

    public long getValorMs() {
        return valorMs;
    }
}
