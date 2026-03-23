package br.edu.fatec.startiot.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotFoundExceptionTest {

    @Test
    void deveCriarComMensagem() {
        NotFoundException ex = new NotFoundException("recurso não encontrado");
        assertThat(ex.getMessage()).isEqualTo("recurso não encontrado");
    }

    @Test
    void deveUsarFactoryMethodOf() {
        NotFoundException ex = NotFoundException.of("Evento", 42L);
        assertThat(ex.getMessage()).isEqualTo("Evento com id 42 não encontrado(a)");
    }

    @Test
    void deveSerRuntimeException() {
        assertThat(new NotFoundException("x")).isInstanceOf(RuntimeException.class);
    }
}
