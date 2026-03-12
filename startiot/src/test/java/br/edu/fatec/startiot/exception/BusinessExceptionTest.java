package br.edu.fatec.startiot.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void deveCriarComMensagem() {
        BusinessException ex = new BusinessException("regra violada");
        assertThat(ex.getMessage()).isEqualTo("regra violada");
    }

    @Test
    void deveSerRuntimeException() {
        assertThat(new BusinessException("x")).isInstanceOf(RuntimeException.class);
    }
}
