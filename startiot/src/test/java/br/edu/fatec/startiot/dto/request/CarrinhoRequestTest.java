package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CarrinhoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<CarrinhoRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new CarrinhoRequest(1L, "CRR-001");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void devePassarSemIdentificacao() {
        var request = new CarrinhoRequest(1L, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarEquipeIdNulo() {
        var request = new CarrinhoRequest(null, "CRR-001");
        assertThat(campos(validator.validate(request))).contains("equipeId");
    }

    @Test
    void deveRejeitarIdentificacaoLongaDemais() {
        var request = new CarrinhoRequest(1L, "X".repeat(51));
        assertThat(campos(validator.validate(request))).contains("identificacao");
    }
}
