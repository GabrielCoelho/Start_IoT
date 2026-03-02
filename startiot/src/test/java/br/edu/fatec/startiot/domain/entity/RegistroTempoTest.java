package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.TipoRegistro;
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
class RegistroTempoTest {

    @Mock
    private Corrida corridaMock;

    @Mock
    private Equipe equipeMock;

    @Mock
    private Usuario usuarioMock;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<RegistroTempo>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarRegistroValido() {
        RegistroTempo registro = new RegistroTempo();
        registro.setCorrida(corridaMock);
        registro.setEquipe(equipeMock);
        registro.setUsuario(usuarioMock);
        registro.setTimestampRegistro(LocalDateTime.now());
        registro.setTempoMilissegundos(45320.5);
        registro.setTipoRegistro(TipoRegistro.CHEGADA);

        Set<ConstraintViolation<RegistroTempo>> violations = validator.validate(registro);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarSemCorrida() {
        RegistroTempo registro = new RegistroTempo();
        registro.setEquipe(equipeMock);
        registro.setUsuario(usuarioMock);
        registro.setTipoRegistro(TipoRegistro.LARGADA);

        Set<ConstraintViolation<RegistroTempo>> violations = validator.validate(registro);

        assertThat(propriedadesVioladas(violations)).contains("corrida");
    }

    @Test
    void deveRejeitarSemEquipe() {
        RegistroTempo registro = new RegistroTempo();
        registro.setCorrida(corridaMock);
        registro.setUsuario(usuarioMock);
        registro.setTipoRegistro(TipoRegistro.LARGADA);

        Set<ConstraintViolation<RegistroTempo>> violations = validator.validate(registro);

        assertThat(propriedadesVioladas(violations)).contains("equipe");
    }

    @Test
    void deveRejeitarSemUsuario() {
        RegistroTempo registro = new RegistroTempo();
        registro.setCorrida(corridaMock);
        registro.setEquipe(equipeMock);
        registro.setTipoRegistro(TipoRegistro.CHEGADA);

        Set<ConstraintViolation<RegistroTempo>> violations = validator.validate(registro);

        assertThat(propriedadesVioladas(violations)).contains("usuario");
    }

    @Test
    void deveRejeitarSemTipoRegistro() {
        RegistroTempo registro = new RegistroTempo();
        registro.setCorrida(corridaMock);
        registro.setEquipe(equipeMock);
        registro.setUsuario(usuarioMock);

        Set<ConstraintViolation<RegistroTempo>> violations = validator.validate(registro);

        assertThat(propriedadesVioladas(violations)).contains("tipoRegistro");
    }

    @Test
    void deveIniciarComoNaoValidadoPorPadrao() {
        RegistroTempo registro = new RegistroTempo();

        assertThat(registro.getValidado()).isFalse();
    }

    @Test
    void deveRegistrarTempoAutomaticoEManual() {
        RegistroTempo automatico = new RegistroTempo();
        automatico.setTipoRegistro(TipoRegistro.AUTOMATICO);

        RegistroTempo manual = new RegistroTempo();
        manual.setTipoRegistro(TipoRegistro.MANUAL);

        assertThat(automatico.getTipoRegistro()).isEqualTo(TipoRegistro.AUTOMATICO);
        assertThat(manual.getTipoRegistro()).isEqualTo(TipoRegistro.MANUAL);
    }
}
