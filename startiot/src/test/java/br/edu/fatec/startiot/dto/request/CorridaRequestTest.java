package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CorridaRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<CorridaRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new CorridaRequest(1L, 1);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarBateriaIdNulo() {
        var request = new CorridaRequest(null, 1);
        assertThat(campos(validator.validate(request))).contains("bateriaId");
    }

    @Test
    void deveRejeitarOrdemNula() {
        var request = new CorridaRequest(1L, null);
        assertThat(campos(validator.validate(request))).contains("ordem");
    }
}
