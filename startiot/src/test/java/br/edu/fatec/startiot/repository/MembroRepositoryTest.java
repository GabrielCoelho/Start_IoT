package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.entity.Membro;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MembroRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MembroRepository membroRepository;

    private Equipe equipe;

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
        equipe.setNome("Team Fatec");
        equipe.setCurso("ADS");
        equipe.setCategoria("A");
        equipe.setStatusInscricao(StatusEquipe.PENDENTE);
        entityManager.persistAndFlush(equipe);
    }

    private Membro criarMembro(String nome, String ra, String funcao) {
        Membro membro = new Membro();
        membro.setEquipe(equipe);
        membro.setNome(nome);
        membro.setRa(ra);
        membro.setFuncao(funcao);
        return membro;
    }

    @Test
    void deveSalvarMembro() {
        Membro membro = criarMembro("Gabriel Coelho", "1234567", "Piloto");
        Membro salvo = membroRepository.save(membro);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Gabriel Coelho");
    }

    @Test
    void deveBuscarPorEquipeId() {
        entityManager.persistAndFlush(criarMembro("Gabriel Coelho", "1234567", "Piloto"));
        entityManager.persistAndFlush(criarMembro("Ana Silva", "7654321", "Mecanico"));

        List<Membro> membros = membroRepository.findByEquipeId(equipe.getId());

        assertThat(membros).hasSize(2);
    }

    @Test
    void deveBuscarPorRa() {
        entityManager.persistAndFlush(criarMembro("Gabriel Coelho", "1234567", "Piloto"));

        Optional<Membro> resultado = membroRepository.findByRa("1234567");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Gabriel Coelho");
    }

    @Test
    void deveRetornarVazioParaRaInexistente() {
        Optional<Membro> resultado = membroRepository.findByRa("0000000");

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveVerificarExistenciaDuplicadaPorRaNaEquipe() {
        entityManager.persistAndFlush(criarMembro("Gabriel Coelho", "1234567", "Piloto"));

        assertThat(membroRepository.existsByRaAndEquipeId("1234567", equipe.getId())).isTrue();
        assertThat(membroRepository.existsByRaAndEquipeId("9999999", equipe.getId())).isFalse();
    }
}
