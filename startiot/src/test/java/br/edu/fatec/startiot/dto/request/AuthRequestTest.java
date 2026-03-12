package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<AuthRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new AuthRequest("user@fatec.br", "senha123");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarEmailEmBranco() {
        var request = new AuthRequest("", "senha123");
        assertThat(campos(validator.validate(request))).contains("email");
    }

    @Test
    void deveRejeitarEmailInvalido() {
        var request = new AuthRequest("nao-e-email", "senha123");
        assertThat(campos(validator.validate(request))).contains("email");
    }

    @Test
    void deveRejeitarSenhaEmBranco() {
        var request = new AuthRequest("user@fatec.br", "");
        assertThat(campos(validator.validate(request))).contains("senha");
    }

    @Test
    void deveRejeitarCamposNulos() {
        var request = new AuthRequest(null, null);
        assertThat(campos(validator.validate(request))).containsExactlyInAnyOrder("email", "senha");
    }
}
