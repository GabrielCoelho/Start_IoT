package br.edu.fatec.startiot.dto.request;

import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EdicaoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<EdicaoRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new EdicaoRequest(1L, 2025, null, LocalDate.of(2025, 6, 15), StatusEdicao.PLANEJADA);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void devePassarSemDataEvento() {
        var request = new EdicaoRequest(1L, 2025, null, null, StatusEdicao.PLANEJADA);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarEventoIdNulo() {
        var request = new EdicaoRequest(null, 2025, null, null, StatusEdicao.PLANEJADA);
        assertThat(campos(validator.validate(request))).contains("eventoId");
    }

    @Test
    void deveRejeitarAnoNulo() {
        var request = new EdicaoRequest(1L, null, null, null, StatusEdicao.PLANEJADA);
        assertThat(campos(validator.validate(request))).contains("ano");
    }

    @Test
    void deveRejeitarStatusNulo() {
        var request = new EdicaoRequest(1L, 2025, null, null, null);
        assertThat(campos(validator.validate(request))).contains("status");
    }
}
