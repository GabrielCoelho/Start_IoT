package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Usuario>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarUsuarioValido() {
        Usuario usuario = new Usuario();
        usuario.setNome("Gabriel Coelho");
        usuario.setEmail("gabriel@fatec.sp.gov.br");
        usuario.setSenhaHash("$2a$10$hash");
        usuario.setPerfil(PerfilUsuario.ORGANIZADOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarEmailInvalido() {
        Usuario usuario = new Usuario();
        usuario.setNome("Gabriel");
        usuario.setEmail("email-invalido");
        usuario.setSenhaHash("hash");
        usuario.setPerfil(PerfilUsuario.CRONOMETRISTA);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);

        assertThat(propriedadesVioladas(violations)).contains("email");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        Usuario usuario = new Usuario();
        usuario.setNome("  ");
        usuario.setEmail("usuario@fatec.sp.gov.br");
        usuario.setSenhaHash("hash");
        usuario.setPerfil(PerfilUsuario.CRONOMETRISTA);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);

        assertThat(propriedadesVioladas(violations)).contains("nome");
    }

    @Test
    void deveRejeitarSenhaNula() {
        Usuario usuario = new Usuario();
        usuario.setNome("Gabriel");
        usuario.setEmail("gabriel@fatec.sp.gov.br");
        usuario.setPerfil(PerfilUsuario.ADMIN);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);

        assertThat(propriedadesVioladas(violations)).contains("senhaHash");
    }

    @Test
    void deveRejeitarSemPerfil() {
        Usuario usuario = new Usuario();
        usuario.setNome("Gabriel");
        usuario.setEmail("gabriel@fatec.sp.gov.br");
        usuario.setSenhaHash("hash");

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);

        assertThat(propriedadesVioladas(violations)).contains("perfil");
    }

    @Test
    void deveIniciarComoAtivoPorPadrao() {
        Usuario usuario = new Usuario();

        assertThat(usuario.getAtivo()).isTrue();
    }
}
