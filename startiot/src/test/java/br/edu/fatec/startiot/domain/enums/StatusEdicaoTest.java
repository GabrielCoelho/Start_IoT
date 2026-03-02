package br.edu.fatec.startiot.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusEdicaoTest {

    @Test
    void deveConterExatamenteQuatroValores() {
        assertThat(StatusEdicao.values()).hasSize(4);
    }

    @Test
    void deveConterTodosOsValoresEsperados() {
        assertThat(StatusEdicao.values()).containsExactlyInAnyOrder(
                StatusEdicao.PLANEJADA,
                StatusEdicao.EM_ANDAMENTO,
                StatusEdicao.FINALIZADA,
                StatusEdicao.CANCELADA
        );
    }

    @Test
    void deveConverterPorNomeCorretamente() {
        assertThat(StatusEdicao.valueOf("PLANEJADA")).isEqualTo(StatusEdicao.PLANEJADA);
        assertThat(StatusEdicao.valueOf("EM_ANDAMENTO")).isEqualTo(StatusEdicao.EM_ANDAMENTO);
        assertThat(StatusEdicao.valueOf("FINALIZADA")).isEqualTo(StatusEdicao.FINALIZADA);
        assertThat(StatusEdicao.valueOf("CANCELADA")).isEqualTo(StatusEdicao.CANCELADA);
    }

    @Test
    void deveRetornarNomeExatoDoValor() {
        assertThat(StatusEdicao.PLANEJADA.name()).isEqualTo("PLANEJADA");
        assertThat(StatusEdicao.EM_ANDAMENTO.name()).isEqualTo("EM_ANDAMENTO");
    }

    @Test
    void deveLancarExcecaoParaValorInexistente() {
        assertThatThrownBy(() -> StatusEdicao.valueOf("INEXISTENTE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
