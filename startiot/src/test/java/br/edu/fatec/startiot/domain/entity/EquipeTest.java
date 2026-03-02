package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EquipeTest {

    @Mock
    private Edicao edicaoMock;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Equipe>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarEquipeValida() {
        Equipe equipe = new Equipe();
        equipe.setEdicao(edicaoMock);
        equipe.setNome("Team Fatec");
        equipe.setCurso("ADS");
        equipe.setCategoria("A");
        equipe.setStatusInscricao(StatusEquipe.PENDENTE);

        Set<ConstraintViolation<Equipe>> violations = validator.validate(equipe);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarSemEdicao() {
        Equipe equipe = new Equipe();
        equipe.setNome("Team Fatec");
        equipe.setStatusInscricao(StatusEquipe.PENDENTE);

        Set<ConstraintViolation<Equipe>> violations = validator.validate(equipe);

        assertThat(propriedadesVioladas(violations)).contains("edicao");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        Equipe equipe = new Equipe();
        equipe.setEdicao(edicaoMock);
        equipe.setNome("");
        equipe.setStatusInscricao(StatusEquipe.PENDENTE);

        Set<ConstraintViolation<Equipe>> violations = validator.validate(equipe);

        assertThat(propriedadesVioladas(violations)).contains("nome");
    }

    @Test
    void deveRejeitarSemStatus() {
        Equipe equipe = new Equipe();
        equipe.setEdicao(edicaoMock);
        equipe.setNome("Team Fatec");

        Set<ConstraintViolation<Equipe>> violations = validator.validate(equipe);

        assertThat(propriedadesVioladas(violations)).contains("statusInscricao");
    }

    @Test
    void deveInicializarListasVazias() {
        Equipe equipe = new Equipe();

        assertThat(equipe.getMembros()).isNotNull().isEmpty();
        assertThat(equipe.getRegistros()).isNotNull().isEmpty();
    }
}
