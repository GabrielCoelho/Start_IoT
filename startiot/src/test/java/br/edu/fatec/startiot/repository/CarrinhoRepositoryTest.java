package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Carrinho;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CarrinhoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    private Equipe equipe;
    private Equipe equipeB;

    @BeforeEach
    void setUp() {
        Evento evento = new Evento();
        evento.setNome("Descida da Ladeira");
        entityManager.persistAndFlush(evento);

        Edicao edicao = new Edicao();
        edicao.setEvento(evento);
        edicao.setAno(2025);
        edicao.setStatus(StatusEdicao.PLANEJADA);
        entityManager.persistAndFlush(edicao);

        equipe = new Equipe();
        equipe.setEdicao(edicao);
        equipe.setNome("Team Alpha");
        equipe.setCurso("ADS");
        equipe.setCategoria("A");
        equipe.setStatusInscricao(StatusEquipe.APROVADA);
        entityManager.persistAndFlush(equipe);

        equipeB = new Equipe();
        equipeB.setEdicao(edicao);
        equipeB.setNome("Team Beta");
        equipeB.setCurso("GTI");
        equipeB.setCategoria("B");
        equipeB.setStatusInscricao(StatusEquipe.APROVADA);
        entityManager.persistAndFlush(equipeB);
    }

    @Test
    void deveSalvarCarrinho() {
        Carrinho carrinho = new Carrinho();
        carrinho.setEquipe(equipe);
        carrinho.setIdentificacao("CART-001");

        Carrinho salvo = carrinhoRepository.save(carrinho);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getAprovadoVistoria()).isFalse();
    }

    @Test
    void deveBuscarPorEquipeId() {
        Carrinho carrinho = new Carrinho();
        carrinho.setEquipe(equipe);
        carrinho.setIdentificacao("CART-001");
        entityManager.persistAndFlush(carrinho);

        Optional<Carrinho> resultado = carrinhoRepository.findByEquipeId(equipe.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIdentificacao()).isEqualTo("CART-001");
    }

    @Test
    void deveBuscarPorAprovacaoDeVistoria() {
        Carrinho aprovado = new Carrinho();
        aprovado.setEquipe(equipe);
        aprovado.setAprovadoVistoria(true);
        aprovado.setDataVistoria(LocalDateTime.now());
        entityManager.persistAndFlush(aprovado);

        Carrinho reprovado = new Carrinho();
        reprovado.setEquipe(equipeB);
        reprovado.setAprovadoVistoria(false);
        entityManager.persistAndFlush(reprovado);

        List<Carrinho> aprovados = carrinhoRepository.findByAprovadoVistoria(true);
        List<Carrinho> reprovados = carrinhoRepository.findByAprovadoVistoria(false);

        assertThat(aprovados).hasSize(1);
        assertThat(reprovados).hasSize(1);
    }

    @Test
    void deveVerificarExistenciaPorEquipeId() {
        Carrinho carrinho = new Carrinho();
        carrinho.setEquipe(equipe);
        entityManager.persistAndFlush(carrinho);

        assertThat(carrinhoRepository.existsByEquipeId(equipe.getId())).isTrue();
        assertThat(carrinhoRepository.existsByEquipeId(equipeB.getId())).isFalse();
    }
}
