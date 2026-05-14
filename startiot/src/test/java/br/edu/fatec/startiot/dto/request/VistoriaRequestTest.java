package br.edu.fatec.startiot.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class VistoriaRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<VistoriaRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComAprovacaoTrue() {
        var request = new VistoriaRequest(true, null, "Tudo ok");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void devePassarComReprovacaoSemObservacoes() {
        var request = new VistoriaRequest(false, null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarAprovadoNulo() {
        var request = new VistoriaRequest(null, null, "obs");
        assertThat(campos(validator.validate(request))).contains("aprovado");
    }
}
