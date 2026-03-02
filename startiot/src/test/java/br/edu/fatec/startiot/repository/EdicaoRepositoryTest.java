package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EdicaoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EdicaoRepository edicaoRepository;

    private Evento evento;

    @BeforeEach
    void setUp() {
        evento = new Evento();
        evento.setNome("Descida da Ladeira");
        entityManager.persistAndFlush(evento);
    }

    private Edicao criarEdicao(int ano, StatusEdicao status) {
        Edicao edicao = new Edicao();
        edicao.setEvento(evento);
        edicao.setAno(ano);
        edicao.setDataEvento(LocalDate.of(ano, 10, 15));
        edicao.setStatus(status);
        return edicao;
    }

    @Test
    void deveSalvarEdicao() {
        Edicao edicao = criarEdicao(2025, StatusEdicao.PLANEJADA);
        Edicao salvo = edicaoRepository.save(edicao);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getAno()).isEqualTo(2025);
        assertThat(salvo.getStatus()).isEqualTo(StatusEdicao.PLANEJADA);
    }

    @Test
    void deveBuscarPorEventoId() {
        entityManager.persistAndFlush(criarEdicao(2025, StatusEdicao.FINALIZADA));
        entityManager.persistAndFlush(criarEdicao(2026, StatusEdicao.PLANEJADA));

        List<Edicao> edicoes = edicaoRepository.findByEventoId(evento.getId());

        assertThat(edicoes).hasSize(2);
    }

    @Test
    void deveBuscarPorStatus() {
        entityManager.persistAndFlush(criarEdicao(2025, StatusEdicao.FINALIZADA));
        entityManager.persistAndFlush(criarEdicao(2026, StatusEdicao.PLANEJADA));

        List<Edicao> planejadas = edicaoRepository.findByStatus(StatusEdicao.PLANEJADA);
        List<Edicao> finalizadas = edicaoRepository.findByStatus(StatusEdicao.FINALIZADA);

        assertThat(planejadas).hasSize(1);
        assertThat(finalizadas).hasSize(1);
    }

    @Test
    void deveBuscarPorEventoIdEAno() {
        entityManager.persistAndFlush(criarEdicao(2025, StatusEdicao.PLANEJADA));

        Optional<Edicao> resultado = edicaoRepository.findByEventoIdAndAno(evento.getId(), 2025);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getAno()).isEqualTo(2025);
    }

    @Test
    void deveRetornarVazioParaAnoInexistente() {
        Optional<Edicao> resultado = edicaoRepository.findByEventoIdAndAno(evento.getId(), 9999);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveVerificarExistenciaPorEventoEAno() {
        entityManager.persistAndFlush(criarEdicao(2025, StatusEdicao.PLANEJADA));

        assertThat(edicaoRepository.existsByEventoIdAndAno(evento.getId(), 2025)).isTrue();
        assertThat(edicaoRepository.existsByEventoIdAndAno(evento.getId(), 2030)).isFalse();
    }
}
