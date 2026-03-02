package br.edu.fatec.startiot.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCorridaTest {

    @Test
    void deveConterExatamenteCincoValores() {
        assertThat(StatusCorrida.values()).hasSize(5);
    }

    @Test
    void deveConterTodosOsValoresEsperados() {
        assertThat(StatusCorrida.values()).containsExactlyInAnyOrder(
                StatusCorrida.AGUARDANDO,
                StatusCorrida.EM_ANDAMENTO,
                StatusCorrida.FINALIZADA,
                StatusCorrida.CANCELADA,
                StatusCorrida.DESCLASSIFICADA
        );
    }

    @Test
    void deveConverterPorNomeCorretamente() {
        assertThat(StatusCorrida.valueOf("DESCLASSIFICADA")).isEqualTo(StatusCorrida.DESCLASSIFICADA);
    }
}
