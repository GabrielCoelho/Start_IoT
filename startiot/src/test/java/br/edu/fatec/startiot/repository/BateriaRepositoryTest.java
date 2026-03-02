package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BateriaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BateriaRepository bateriaRepository;

    private Edicao edicao;

    @BeforeEach
    void setUp() {
        Evento evento = new Evento();
        evento.setNome("Descida da Ladeira");
        entityManager.persistAndFlush(evento);

        edicao = new Edicao();
        edicao.setEvento(evento);
        edicao.setAno(2025);
        edicao.setStatus(StatusEdicao.EM_ANDAMENTO);
        entityManager.persistAndFlush(edicao);
    }

    private Bateria criarBateria(int numero, String tipo, StatusBateria status) {
        Bateria bateria = new Bateria();
        bateria.setEdicao(edicao);
        bateria.setNumero(numero);
        bateria.setTipo(tipo);
        bateria.setHorarioPrevisto(LocalDateTime.of(2025, 10, 15, 9 + numero, 0));
        bateria.setStatus(status);
        return bateria;
    }

    @Test
    void deveSalvarBateria() {
        Bateria bateria = criarBateria(1, "Classificatória", StatusBateria.AGUARDANDO);
        Bateria salvo = bateriaRepository.save(bateria);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNumero()).isEqualTo(1);
    }

    @Test
    void deveBuscarPorEdicaoIdOrdenadoPorNumero() {
        entityManager.persistAndFlush(criarBateria(3, "Final", StatusBateria.AGUARDANDO));
        entityManager.persistAndFlush(criarBateria(1, "Classificatória", StatusBateria.FINALIZADA));
        entityManager.persistAndFlush(criarBateria(2, "Semifinal", StatusBateria.AGUARDANDO));

        List<Bateria> baterias = bateriaRepository.findByEdicaoIdOrderByNumero(edicao.getId());

        assertThat(baterias).hasSize(3);
        assertThat(baterias.get(0).getNumero()).isEqualTo(1);
        assertThat(baterias.get(1).getNumero()).isEqualTo(2);
        assertThat(baterias.get(2).getNumero()).isEqualTo(3);
    }

    @Test
    void deveBuscarPorEdicaoIdEStatus() {
        entityManager.persistAndFlush(criarBateria(1, "Classificatória", StatusBateria.FINALIZADA));
        entityManager.persistAndFlush(criarBateria(2, "Semifinal", StatusBateria.AGUARDANDO));
        entityManager.persistAndFlush(criarBateria(3, "Final", StatusBateria.AGUARDANDO));

        List<Bateria> aguardando = bateriaRepository.findByEdicaoIdAndStatus(edicao.getId(), StatusBateria.AGUARDANDO);

        assertThat(aguardando).hasSize(2);
    }

    @Test
    void deveVerificarExistenciaPorEdicaoENumero() {
        entityManager.persistAndFlush(criarBateria(1, "Classificatória", StatusBateria.AGUARDANDO));

        assertThat(bateriaRepository.existsByEdicaoIdAndNumero(edicao.getId(), 1)).isTrue();
        assertThat(bateriaRepository.existsByEdicaoIdAndNumero(edicao.getId(), 99)).isFalse();
    }
}
