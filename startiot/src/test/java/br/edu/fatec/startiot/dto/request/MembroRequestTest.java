package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MembroRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<MembroRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new MembroRequest(1L, "Ana Paula", "ADS2024001", "piloto");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void devePassarSemRaEFuncao() {
        var request = new MembroRequest(1L, "Ana Paula", null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarEquipeIdNulo() {
        var request = new MembroRequest(null, "Ana Paula", "001", "piloto");
        assertThat(campos(validator.validate(request))).contains("equipeId");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        var request = new MembroRequest(1L, "", "001", "piloto");
        assertThat(campos(validator.validate(request))).contains("nome");
    }

    @Test
    void deveRejeitarRaLongoDemais() {
        var request = new MembroRequest(1L, "Ana", "R".repeat(21), "piloto");
        assertThat(campos(validator.validate(request))).contains("ra");
    }
}
