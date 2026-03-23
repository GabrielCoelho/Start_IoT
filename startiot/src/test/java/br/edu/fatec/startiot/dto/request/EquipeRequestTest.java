package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EquipeRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<EquipeRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new EquipeRequest(1L, "Team Fatec", "ADS", "A");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void devePassarSemCursoECategoria() {
        var request = new EquipeRequest(1L, "Team Fatec", null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarEdicaoIdNulo() {
        var request = new EquipeRequest(null, "Team Fatec", "ADS", "A");
        assertThat(campos(validator.validate(request))).contains("edicaoId");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        var request = new EquipeRequest(1L, "", "ADS", "A");
        assertThat(campos(validator.validate(request))).contains("nome");
    }

    @Test
    void deveRejeitarNomeLongoDemais() {
        var request = new EquipeRequest(1L, "N".repeat(101), "ADS", "A");
        assertThat(campos(validator.validate(request))).contains("nome");
    }
}
