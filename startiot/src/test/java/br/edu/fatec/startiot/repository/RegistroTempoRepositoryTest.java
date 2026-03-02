package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.*;
import br.edu.fatec.startiot.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RegistroTempoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RegistroTempoRepository registroTempoRepository;

    private Corrida corrida;
    private Equipe equipe;
    private Equipe equipeB;
    private Usuario usuario;

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
        equipeB.setCategoria("A");
        equipeB.setStatusInscricao(StatusEquipe.APROVADA);
        entityManager.persistAndFlush(equipeB);

        Bateria bateria = new Bateria();
        bateria.setEdicao(edicao);
        bateria.setNumero(1);
        bateria.setTipo("Classificatória");
        bateria.setStatus(StatusBateria.EM_ANDAMENTO);
        entityManager.persistAndFlush(bateria);

        corrida = new Corrida();
        corrida.setBateria(bateria);
        corrida.setOrdem(1);
        corrida.setStatus(StatusCorrida.EM_ANDAMENTO);
        entityManager.persistAndFlush(corrida);

        usuario = new Usuario();
        usuario.setNome("Cronometrista Teste");
        usuario.setEmail("cronometrista@fatec.sp.gov.br");
        usuario.setSenhaHash("$2a$10$hash");
        usuario.setPerfil(PerfilUsuario.CRONOMETRISTA);
        entityManager.persistAndFlush(usuario);
    }

    private RegistroTempo criarRegistro(Equipe eq, TipoRegistro tipo, Double tempo, Boolean validado) {
        RegistroTempo registro = new RegistroTempo();
        registro.setCorrida(corrida);
        registro.setEquipe(eq);
        registro.setUsuario(usuario);
        registro.setTimestampRegistro(LocalDateTime.now());
        registro.setTempoMilissegundos(tempo);
        registro.setTipoRegistro(tipo);
        registro.setValidado(validado);
        return registro;
    }

    @Test
    void deveSalvarRegistroTempo() {
        RegistroTempo registro = criarRegistro(equipe, TipoRegistro.CHEGADA, 45320.5, false);
        RegistroTempo salvo = registroTempoRepository.save(registro);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getValidado()).isFalse();
        assertThat(salvo.getTempoMilissegundos()).isEqualTo(45320.5);
    }

    @Test
    void deveBuscarPorCorridaId() {
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.LARGADA, null, false));
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.CHEGADA, 45320.5, false));
        entityManager.persistAndFlush(criarRegistro(equipeB, TipoRegistro.CHEGADA, 48750.0, false));

        List<RegistroTempo> registros = registroTempoRepository.findByCorridaId(corrida.getId());

        assertThat(registros).hasSize(3);
    }

    @Test
    void deveBuscarPorEquipeECorrida() {
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.LARGADA, null, false));
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.CHEGADA, 45320.5, true));
        entityManager.persistAndFlush(criarRegistro(equipeB, TipoRegistro.CHEGADA, 48750.0, false));

        List<RegistroTempo> registrosDaEquipe = registroTempoRepository
                .findByEquipeIdAndCorridaId(equipe.getId(), corrida.getId());

        assertThat(registrosDaEquipe).hasSize(2);
    }

    @Test
    void deveBuscarPorCorridaEtipo() {
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.LARGADA, null, false));
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.CHEGADA, 45320.5, true));
        entityManager.persistAndFlush(criarRegistro(equipeB, TipoRegistro.CHEGADA, 48750.0, false));

        List<RegistroTempo> chegadas = registroTempoRepository
                .findByCorridaIdAndTipoRegistro(corrida.getId(), TipoRegistro.CHEGADA);

        assertThat(chegadas).hasSize(2);
    }

    @Test
    void deveBuscarNaoValidados() {
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.CHEGADA, 45320.5, false));
        entityManager.persistAndFlush(criarRegistro(equipeB, TipoRegistro.CHEGADA, 48750.0, true));

        List<RegistroTempo> naoValidados = registroTempoRepository.findByValidado(false);

        assertThat(naoValidados).hasSize(1);
    }

    @Test
    void deveBuscarTemposDaEquipeOrdenadosMenorParaMaior() {
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.CHEGADA, 50000.0, true));
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.MANUAL, 45320.5, true));
        entityManager.persistAndFlush(criarRegistro(equipe, TipoRegistro.CHEGADA, 47800.0, true));

        List<RegistroTempo> tempos = registroTempoRepository
                .findByEquipeIdOrderByTempoMilissegundosAsc(equipe.getId());

        assertThat(tempos).hasSize(3);
        assertThat(tempos.get(0).getTempoMilissegundos()).isEqualTo(45320.5);
        assertThat(tempos.get(2).getTempoMilissegundos()).isEqualTo(50000.0);
    }
}
