package br.edu.fatec.startiot.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusBateriaTest {

    @Test
    void deveConterExatamenteQuatroValores() {
        assertThat(StatusBateria.values()).hasSize(4);
    }

    @Test
    void deveConterTodosOsValoresEsperados() {
        assertThat(StatusBateria.values()).containsExactlyInAnyOrder(
                StatusBateria.AGUARDANDO,
                StatusBateria.EM_ANDAMENTO,
                StatusBateria.FINALIZADA,
                StatusBateria.CANCELADA
        );
    }

    @Test
    void deveConverterPorNomeCorretamente() {
        assertThat(StatusBateria.valueOf("AGUARDANDO")).isEqualTo(StatusBateria.AGUARDANDO);
        assertThat(StatusBateria.valueOf("EM_ANDAMENTO")).isEqualTo(StatusBateria.EM_ANDAMENTO);
        assertThat(StatusBateria.valueOf("FINALIZADA")).isEqualTo(StatusBateria.FINALIZADA);
        assertThat(StatusBateria.valueOf("CANCELADA")).isEqualTo(StatusBateria.CANCELADA);
    }
}
