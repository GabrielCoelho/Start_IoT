package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EdicaoTest {

    @Mock
    private Evento eventoMock;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Edicao>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarEdicaoValida() {
        Edicao edicao = new Edicao();
        edicao.setEvento(eventoMock);
        edicao.setAno(2025);
        edicao.setDataEvento(LocalDate.of(2025, 10, 15));
        edicao.setStatus(StatusEdicao.PLANEJADA);

        Set<ConstraintViolation<Edicao>> violations = validator.validate(edicao);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarSemEvento() {
        Edicao edicao = new Edicao();
        edicao.setAno(2025);
        edicao.setStatus(StatusEdicao.PLANEJADA);

        Set<ConstraintViolation<Edicao>> violations = validator.validate(edicao);

        assertThat(propriedadesVioladas(violations)).contains("evento");
    }

    @Test
    void deveRejeitarSemAno() {
        Edicao edicao = new Edicao();
        edicao.setEvento(eventoMock);
        edicao.setStatus(StatusEdicao.PLANEJADA);

        Set<ConstraintViolation<Edicao>> violations = validator.validate(edicao);

        assertThat(propriedadesVioladas(violations)).contains("ano");
    }

    @Test
    void deveRejeitarSemStatus() {
        Edicao edicao = new Edicao();
        edicao.setEvento(eventoMock);
        edicao.setAno(2025);

        Set<ConstraintViolation<Edicao>> violations = validator.validate(edicao);

        assertThat(propriedadesVioladas(violations)).contains("status");
    }

    @Test
    void deveInicializarListasVazias() {
        Edicao edicao = new Edicao();

        assertThat(edicao.getEquipes()).isNotNull().isEmpty();
        assertThat(edicao.getBaterias()).isNotNull().isEmpty();
    }

    @Test
    void deveAssociarEventoMockado() {
        Edicao edicao = new Edicao();
        edicao.setEvento(eventoMock);

        assertThat(edicao.getEvento()).isEqualTo(eventoMock);
    }
}
