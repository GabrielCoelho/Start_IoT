package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EventoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<EventoRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComNomeValido() {
        var request = new EventoRequest("Descida da Ladeira", "Evento anual da Fatec");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void devePassarSemDescricao() {
        var request = new EventoRequest("Descida da Ladeira", null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        var request = new EventoRequest("", "desc");
        assertThat(campos(validator.validate(request))).contains("nome");
    }

    @Test
    void deveRejeitarNomeCurtoDemais() {
        var request = new EventoRequest("AB", "desc");
        assertThat(campos(validator.validate(request))).contains("nome");
    }

    @Test
    void deveRejeitarNomeLongoDemais() {
        var request = new EventoRequest("A".repeat(101), "desc");
        assertThat(campos(validator.validate(request))).contains("nome");
    }
}
