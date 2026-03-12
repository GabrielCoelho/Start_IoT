package br.edu.fatec.startiot.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConflictExceptionTest {

    @Test
    void deveCriarComMensagem() {
        ConflictException ex = new ConflictException("dado duplicado");
        assertThat(ex.getMessage()).isEqualTo("dado duplicado");
    }

    @Test
    void deveSerRuntimeException() {
        assertThat(new ConflictException("x")).isInstanceOf(RuntimeException.class);
    }
}
