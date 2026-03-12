package br.edu.fatec.startiot.dto.request;

import br.edu.fatec.startiot.domain.enums.TipoRegistro;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RegistroTempoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<RegistroTempoRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new RegistroTempoRequest(1L, 2L, 15234.5, TipoRegistro.CHEGADA, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarCorridaIdNulo() {
        var request = new RegistroTempoRequest(null, 2L, 15234.5, TipoRegistro.CHEGADA, null);
        assertThat(campos(validator.validate(request))).contains("corridaId");
    }

    @Test
    void deveRejeitarEquipeIdNulo() {
        var request = new RegistroTempoRequest(1L, null, 15234.5, TipoRegistro.CHEGADA, null);
        assertThat(campos(validator.validate(request))).contains("equipeId");
    }

    @Test
    void deveRejeitarTempoNulo() {
        var request = new RegistroTempoRequest(1L, 2L, null, TipoRegistro.CHEGADA, null);
        assertThat(campos(validator.validate(request))).contains("tempoMilissegundos");
    }

    @Test
    void deveRejeitarTempoNegativo() {
        var request = new RegistroTempoRequest(1L, 2L, -1.0, TipoRegistro.CHEGADA, null);
        assertThat(campos(validator.validate(request))).contains("tempoMilissegundos");
    }

    @Test
    void deveRejeitarTipoRegistroNulo() {
        var request = new RegistroTempoRequest(1L, 2L, 15234.5, null, null);
        assertThat(campos(validator.validate(request))).contains("tipoRegistro");
    }
}
