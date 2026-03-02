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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CarrinhoTest {

    @Mock
    private Equipe equipeMock;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<String> propriedadesVioladas(Set<ConstraintViolation<Carrinho>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void deveCriarCarrinhoValido() {
        Carrinho carrinho = new Carrinho();
        carrinho.setEquipe(equipeMock);
        carrinho.setIdentificacao("CART-001");
        carrinho.setAprovadoVistoria(false);
        carrinho.setObservacoesVistoria("Aguardando inspeção");
        carrinho.setDataVistoria(LocalDateTime.now());

        Set<ConstraintViolation<Carrinho>> violations = validator.validate(carrinho);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveCriarCarrinhoMinimoValido() {
        Carrinho carrinho = new Carrinho();
        carrinho.setEquipe(equipeMock);

        Set<ConstraintViolation<Carrinho>> violations = validator.validate(carrinho);

        assertThat(violations).isEmpty();
    }

    @Test
    void deveRejeitarSemEquipe() {
        Carrinho carrinho = new Carrinho();

        Set<ConstraintViolation<Carrinho>> violations = validator.validate(carrinho);

        assertThat(propriedadesVioladas(violations)).contains("equipe");
    }

    @Test
    void deveIniciarComAprovacaoFalsaPorPadrao() {
        Carrinho carrinho = new Carrinho();

        assertThat(carrinho.getAprovadoVistoria()).isFalse();
    }

    @Test
    void deveRegistrarAprovacaoDeVistoria() {
        Carrinho carrinho = new Carrinho();
        carrinho.setEquipe(equipeMock);
        carrinho.setAprovadoVistoria(true);
        carrinho.setDataVistoria(LocalDateTime.now());

        assertThat(carrinho.getAprovadoVistoria()).isTrue();
        assertThat(carrinho.getDataVistoria()).isNotNull();
    }
}
