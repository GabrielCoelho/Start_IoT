package br.edu.fatec.startiot.repository;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EquipeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EquipeRepository equipeRepository;

    private Edicao edicao;

    @BeforeEach
    void setUp() {
        Evento evento = new Evento();
        evento.setNome("Descida da Ladeira");
        entityManager.persistAndFlush(evento);

        edicao = new Edicao();
        edicao.setEvento(evento);
        edicao.setAno(2025);
        edicao.setStatus(StatusEdicao.PLANEJADA);
        entityManager.persistAndFlush(edicao);
    }

    private Equipe criarEquipe(String nome, String categoria, StatusEquipe status) {
        Equipe equipe = new Equipe();
        equipe.setEdicao(edicao);
        equipe.setNome(nome);
        equipe.setCurso("ADS");
        equipe.setCategoria(categoria);
        equipe.setStatusInscricao(status);
        return equipe;
    }

    @Test
    void deveSalvarEquipe() {
        Equipe equipe = criarEquipe("Team Fatec", "A", StatusEquipe.PENDENTE);
        Equipe salvo = equipeRepository.save(equipe);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Team Fatec");
    }

    @Test
    void deveBuscarPorEdicaoId() {
        entityManager.persistAndFlush(criarEquipe("Team Alpha", "A", StatusEquipe.APROVADA));
        entityManager.persistAndFlush(criarEquipe("Team Beta", "B", StatusEquipe.PENDENTE));

        List<Equipe> equipes = equipeRepository.findByEdicaoId(edicao.getId());

        assertThat(equipes).hasSize(2);
    }

    @Test
    void deveBuscarPorEdicaoIdEStatus() {
        entityManager.persistAndFlush(criarEquipe("Team Alpha", "A", StatusEquipe.APROVADA));
        entityManager.persistAndFlush(criarEquipe("Team Beta", "B", StatusEquipe.PENDENTE));
        entityManager.persistAndFlush(criarEquipe("Team Gamma", "A", StatusEquipe.APROVADA));

        List<Equipe> aprovadas = equipeRepository.findByEdicaoIdAndStatusInscricao(edicao.getId(), StatusEquipe.APROVADA);

        assertThat(aprovadas).hasSize(2);
    }

    @Test
    void deveBuscarPorEdicaoIdECategoria() {
        entityManager.persistAndFlush(criarEquipe("Team Alpha", "A", StatusEquipe.APROVADA));
        entityManager.persistAndFlush(criarEquipe("Team Beta", "B", StatusEquipe.APROVADA));
        entityManager.persistAndFlush(criarEquipe("Team Gamma", "A", StatusEquipe.APROVADA));

        List<Equipe> categoriaA = equipeRepository.findByEdicaoIdAndCategoria(edicao.getId(), "A");

        assertThat(categoriaA).hasSize(2);
    }

    @Test
    void deveVerificarExistenciaPorEdicaoENome() {
        entityManager.persistAndFlush(criarEquipe("Team Fatec", "A", StatusEquipe.PENDENTE));

        assertThat(equipeRepository.existsByEdicaoIdAndNome(edicao.getId(), "Team Fatec")).isTrue();
        assertThat(equipeRepository.existsByEdicaoIdAndNome(edicao.getId(), "Team Inexistente")).isFalse();
    }
}
