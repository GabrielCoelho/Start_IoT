package br.edu.fatec.startiot.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TipoRegistroTest {

    @Test
    void deveConterExatamenteQuatroValores() {
        assertThat(TipoRegistro.values()).hasSize(4);
    }

    @Test
    void deveConterTodosOsValoresEsperados() {
        assertThat(TipoRegistro.values()).containsExactlyInAnyOrder(
                TipoRegistro.LARGADA,
                TipoRegistro.CHEGADA,
                TipoRegistro.MANUAL,
                TipoRegistro.AUTOMATICO
        );
    }

    @Test
    void deveConterTiposDoSistemaHibrido() {
        assertThat(TipoRegistro.values())
                .contains(TipoRegistro.AUTOMATICO)
                .contains(TipoRegistro.MANUAL);
    }

    @Test
    void deveConterEventosDeLargadaEChegada() {
        assertThat(TipoRegistro.values())
                .contains(TipoRegistro.LARGADA)
                .contains(TipoRegistro.CHEGADA);
    }
}
