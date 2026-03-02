package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusCorrida;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CorridaTest {

    @Mock
    private Bateria bateriaMock;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Corrida>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarCorridaValida() {
        Corrida corrida = new Corrida();
        corrida.setBateria(bateriaMock);
        corrida.setOrdem(1);
        corrida.setStatus(StatusCorrida.AGUARDANDO);

        Set<ConstraintViolation<Corrida>> violations = validator.validate(corrida);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveCriarCorridaComHorarios() {
        Corrida corrida = new Corrida();
        corrida.setBateria(bateriaMock);
        corrida.setOrdem(1);
        corrida.setStatus(StatusCorrida.FINALIZADA);
        corrida.setHorarioInicio(LocalDateTime.of(2025, 10, 15, 9, 0));
        corrida.setHorarioFim(LocalDateTime.of(2025, 10, 15, 9, 1));

        Set<ConstraintViolation<Corrida>> violations = validator.validate(corrida);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarSemBateria() {
        Corrida corrida = new Corrida();
        corrida.setOrdem(1);
        corrida.setStatus(StatusCorrida.AGUARDANDO);

        Set<ConstraintViolation<Corrida>> violations = validator.validate(corrida);

        assertThat(propriedadesVioladas(violations)).contains("bateria");
    }

    @Test
    void deveRejeitarSemStatus() {
        Corrida corrida = new Corrida();
        corrida.setBateria(bateriaMock);
        corrida.setOrdem(1);

        Set<ConstraintViolation<Corrida>> violations = validator.validate(corrida);

        assertThat(propriedadesVioladas(violations)).contains("status");
    }

    @Test
    void deveInicializarListaDeRegistrosVazia() {
        Corrida corrida = new Corrida();

        assertThat(corrida.getRegistros()).isNotNull().isEmpty();
    }
}
