package br.edu.fatec.startiot.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusEquipeTest {

    @Test
    void deveConterExatamenteQuatroValores() {
        assertThat(StatusEquipe.values()).hasSize(4);
    }

    @Test
    void deveConterTodosOsValoresEsperados() {
        assertThat(StatusEquipe.values()).containsExactlyInAnyOrder(
                StatusEquipe.PENDENTE,
                StatusEquipe.APROVADA,
                StatusEquipe.REPROVADA,
                StatusEquipe.CANCELADA
        );
    }

    @Test
    void deveConverterPorNomeCorretamente() {
        assertThat(StatusEquipe.valueOf("PENDENTE")).isEqualTo(StatusEquipe.PENDENTE);
        assertThat(StatusEquipe.valueOf("APROVADA")).isEqualTo(StatusEquipe.APROVADA);
        assertThat(StatusEquipe.valueOf("REPROVADA")).isEqualTo(StatusEquipe.REPROVADA);
        assertThat(StatusEquipe.valueOf("CANCELADA")).isEqualTo(StatusEquipe.CANCELADA);
    }
}
