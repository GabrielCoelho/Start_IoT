package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Evento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventoRepository eventoRepository;

    private Evento evento;

    @BeforeEach
    void setUp() {
        evento = new Evento();
        evento.setNome("Descida da Ladeira");
        evento.setDescricao("Evento anual de carrinho de rolimã");
    }

    @Test
    void deveSalvarEvento() {
        Evento salvo = eventoRepository.save(evento);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Descida da Ladeira");
    }

    @Test
    void devePreencherDataCriacaoAutomaticamente() {
        Evento salvo = eventoRepository.save(evento);
        entityManager.flush();
        entityManager.clear();

        Evento encontrado = eventoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getDataCriacao()).isNotNull();
    }

    @Test
    void deveBuscarPorNome() {
        entityManager.persistAndFlush(evento);

        Optional<Evento> resultado = eventoRepository.findByNome("Descida da Ladeira");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Descida da Ladeira");
    }

    @Test
    void deveRetornarVazioParaNomeInexistente() {
        Optional<Evento> resultado = eventoRepository.findByNome("Evento Inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveVerificarExistenciaPorNome() {
        entityManager.persistAndFlush(evento);

        assertThat(eventoRepository.existsByNome("Descida da Ladeira")).isTrue();
        assertThat(eventoRepository.existsByNome("Outro Evento")).isFalse();
    }

    @Test
    void deveListarTodosOsEventos() {
        entityManager.persistAndFlush(evento);

        Evento outro = new Evento();
        outro.setNome("Corrida de Drones");
        entityManager.persistAndFlush(outro);

        List<Evento> eventos = eventoRepository.findAll();

        assertThat(eventos).hasSize(2);
    }

    @Test
    void deveDeletarEvento() {
        Evento salvo = entityManager.persistAndFlush(evento);

        eventoRepository.deleteById(salvo.getId());

        assertThat(eventoRepository.findById(salvo.getId())).isEmpty();
    }
}
