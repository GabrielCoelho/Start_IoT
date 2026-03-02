package br.edu.fatec.startiot.domain.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EventoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Evento>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarEventoValido() {
        Evento evento = new Evento();
        evento.setNome("Descida da Ladeira");
        evento.setDescricao("Evento anual de carrinho de rolimã da Fatec Mogi Mirim");

        Set<ConstraintViolation<Evento>> violations = validator.validate(evento);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveCriarEventoSemDescricao() {
        Evento evento = new Evento();
        evento.setNome("Descida da Ladeira");

        Set<ConstraintViolation<Evento>> violations = validator.validate(evento);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        Evento evento = new Evento();
        evento.setNome("   ");

        Set<ConstraintViolation<Evento>> violations = validator.validate(evento);

        assertThat(propriedadesVioladas(violations)).contains("nome");
    }

    @Test
    void deveRejeitarNomeNulo() {
        Evento evento = new Evento();

        Set<ConstraintViolation<Evento>> violations = validator.validate(evento);

        assertThat(propriedadesVioladas(violations)).contains("nome");
    }

    @Test
    void deveRejeitarNomeMenorQueTresCaracteres() {
        Evento evento = new Evento();
        evento.setNome("AB");

        Set<ConstraintViolation<Evento>> violations = validator.validate(evento);

        assertThat(propriedadesVioladas(violations)).contains("nome");
    }

    @Test
    void deveInicializarListaDeEdicoesVazia() {
        Evento evento = new Evento();

        assertThat(evento.getEdicoes()).isNotNull().isEmpty();
    }
}
