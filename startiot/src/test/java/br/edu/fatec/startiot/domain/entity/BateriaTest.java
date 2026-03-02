package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusBateria;
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
class BateriaTest {

    @Mock
    private Edicao edicaoMock;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Bateria>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarBateriaValida() {
        Bateria bateria = new Bateria();
        bateria.setEdicao(edicaoMock);
        bateria.setNumero(1);
        bateria.setTipo("Classificatória");
        bateria.setHorarioPrevisto(LocalDateTime.of(2025, 10, 15, 9, 0));
        bateria.setStatus(StatusBateria.AGUARDANDO);

        Set<ConstraintViolation<Bateria>> violations = validator.validate(bateria);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarSemEdicao() {
        Bateria bateria = new Bateria();
        bateria.setNumero(1);
        bateria.setStatus(StatusBateria.AGUARDANDO);

        Set<ConstraintViolation<Bateria>> violations = validator.validate(bateria);

        assertThat(propriedadesVioladas(violations)).contains("edicao");
    }

    @Test
    void deveRejeitarSemStatus() {
        Bateria bateria = new Bateria();
        bateria.setEdicao(edicaoMock);
        bateria.setNumero(1);

        Set<ConstraintViolation<Bateria>> violations = validator.validate(bateria);

        assertThat(propriedadesVioladas(violations)).contains("status");
    }

    @Test
    void deveInicializarListaDeCorridasVazia() {
        Bateria bateria = new Bateria();

        assertThat(bateria.getCorridas()).isNotNull().isEmpty();
    }

    @Test
    void deveAssociarEdicaoMockada() {
        Bateria bateria = new Bateria();
        bateria.setEdicao(edicaoMock);

        assertThat(bateria.getEdicao()).isEqualTo(edicaoMock);
    }
}
