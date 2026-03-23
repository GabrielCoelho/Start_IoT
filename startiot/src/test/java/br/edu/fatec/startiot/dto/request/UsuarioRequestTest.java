package br.edu.fatec.startiot.dto.request;

import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> campos(Set<ConstraintViolation<UsuarioRequest>> v) {
        return v.stream().map(c -> c.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void devePassarComDadosValidos() {
        var request = new UsuarioRequest("João Silva", "joao@fatec.br", "senha1234", PerfilUsuario.CRONOMETRISTA);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        var request = new UsuarioRequest("", "joao@fatec.br", "senha1234", PerfilUsuario.CRONOMETRISTA);
        assertThat(campos(validator.validate(request))).contains("nome");
    }

    @Test
    void deveRejeitarEmailInvalido() {
        var request = new UsuarioRequest("João", "invalido", "senha1234", PerfilUsuario.CRONOMETRISTA);
        assertThat(campos(validator.validate(request))).contains("email");
    }

    @Test
    void deveRejeitarSenhaCurtaDemais() {
        var request = new UsuarioRequest("João", "joao@fatec.br", "123", PerfilUsuario.CRONOMETRISTA);
        assertThat(campos(validator.validate(request))).contains("senha");
    }

    @Test
    void deveRejeitarPerfilNulo() {
        var request = new UsuarioRequest("João", "joao@fatec.br", "senha1234", null);
        assertThat(campos(validator.validate(request))).contains("perfil");
    }
}
