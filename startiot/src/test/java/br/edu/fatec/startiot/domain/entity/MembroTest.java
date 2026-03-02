package br.edu.fatec.startiot.domain.entity;

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
class MembroTest {

    @Mock
    private Equipe equipeMock;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Membro>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarMembroValido() {
        Membro membro = new Membro();
        membro.setEquipe(equipeMock);
        membro.setNome("Gabriel Coelho");
        membro.setRa("1234567");
        membro.setFuncao("Piloto");

        Set<ConstraintViolation<Membro>> violations = validator.validate(membro);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveCriarMembroSemRaEFuncao() {
        Membro membro = new Membro();
        membro.setEquipe(equipeMock);
        membro.setNome("Gabriel Coelho");

        Set<ConstraintViolation<Membro>> violations = validator.validate(membro);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarSemEquipe() {
        Membro membro = new Membro();
        membro.setNome("Gabriel Coelho");

        Set<ConstraintViolation<Membro>> violations = validator.validate(membro);

        assertThat(propriedadesVioladas(violations)).contains("equipe");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        Membro membro = new Membro();
        membro.setEquipe(equipeMock);
        membro.setNome("  ");

        Set<ConstraintViolation<Membro>> violations = validator.validate(membro);

        assertThat(propriedadesVioladas(violations)).contains("nome");
    }

    @Test
    void deveAssociarEquipeMockada() {
        Membro membro = new Membro();
        membro.setEquipe(equipeMock);

        assertThat(membro.getEquipe()).isEqualTo(equipeMock);
    }
}
