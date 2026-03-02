package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Corrida;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import br.edu.fatec.startiot.domain.enums.StatusCorrida;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CorridaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CorridaRepository corridaRepository;

    private Bateria bateria;

    @BeforeEach
    void setUp() {
        Evento evento = new Evento();
        evento.setNome("Descida da Ladeira");
        entityManager.persistAndFlush(evento);

        Edicao edicao = new Edicao();
        edicao.setEvento(evento);
        edicao.setAno(2025);
        edicao.setStatus(StatusEdicao.EM_ANDAMENTO);
        entityManager.persistAndFlush(edicao);

        bateria = new Bateria();
        bateria.setEdicao(edicao);
        bateria.setNumero(1);
        bateria.setTipo("Classificatória");
        bateria.setStatus(StatusBateria.EM_ANDAMENTO);
        entityManager.persistAndFlush(bateria);
    }

    private Corrida criarCorrida(int ordem, StatusCorrida status) {
        Corrida corrida = new Corrida();
        corrida.setBateria(bateria);
        corrida.setOrdem(ordem);
        corrida.setStatus(status);
        return corrida;
    }

    @Test
    void deveSalvarCorrida() {
        Corrida corrida = criarCorrida(1, StatusCorrida.AGUARDANDO);
        Corrida salvo = corridaRepository.save(corrida);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getOrdem()).isEqualTo(1);
    }

    @Test
    void deveBuscarPorBateriaIdOrdenadoPorOrdem() {
        entityManager.persistAndFlush(criarCorrida(3, StatusCorrida.AGUARDANDO));
        entityManager.persistAndFlush(criarCorrida(1, StatusCorrida.FINALIZADA));
        entityManager.persistAndFlush(criarCorrida(2, StatusCorrida.EM_ANDAMENTO));

        List<Corrida> corridas = corridaRepository.findByBateriaIdOrderByOrdem(bateria.getId());

        assertThat(corridas).hasSize(3);
        assertThat(corridas.get(0).getOrdem()).isEqualTo(1);
        assertThat(corridas.get(1).getOrdem()).isEqualTo(2);
        assertThat(corridas.get(2).getOrdem()).isEqualTo(3);
    }

    @Test
    void deveBuscarPorBateriaIdEStatus() {
        entityManager.persistAndFlush(criarCorrida(1, StatusCorrida.FINALIZADA));
        entityManager.persistAndFlush(criarCorrida(2, StatusCorrida.AGUARDANDO));
        entityManager.persistAndFlush(criarCorrida(3, StatusCorrida.AGUARDANDO));

        List<Corrida> aguardando = corridaRepository.findByBateriaIdAndStatus(bateria.getId(), StatusCorrida.AGUARDANDO);

        assertThat(aguardando).hasSize(2);
    }

    @Test
    void deveVerificarExistenciaPorBateriaEOrdem() {
        entityManager.persistAndFlush(criarCorrida(1, StatusCorrida.AGUARDANDO));

        assertThat(corridaRepository.existsByBateriaIdAndOrdem(bateria.getId(), 1)).isTrue();
        assertThat(corridaRepository.existsByBateriaIdAndOrdem(bateria.getId(), 99)).isFalse();
    }
}
