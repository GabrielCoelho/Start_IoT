package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BateriaRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<BateriaRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new BateriaRequest(1L, 1, "eliminatoria", LocalDateTime.now().plusHours(1));
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void devePassarSemTipoEHorario() {
        var request = new BateriaRequest(1L, 1, null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarEdicaoIdNulo() {
        var request = new BateriaRequest(null, 1, "elim", null);
        assertThat(campos(validator.validate(request))).contains("edicaoId");
    }

    @Test
    void deveRejeitarNumeroNulo() {
        var request = new BateriaRequest(1L, null, "elim", null);
        assertThat(campos(validator.validate(request))).contains("numero");
    }

    @Test
    void deveRejeitarTipoLongoDemais() {
        var request = new BateriaRequest(1L, 1, "T".repeat(51), null);
        assertThat(campos(validator.validate(request))).contains("tipo");
    }
}
