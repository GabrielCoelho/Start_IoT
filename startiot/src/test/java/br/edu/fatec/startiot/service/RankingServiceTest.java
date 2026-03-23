package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.*;
import br.edu.fatec.startiot.domain.enums.*;
import br.edu.fatec.startiot.dto.response.RankingResponse;
import br.edu.fatec.startiot.repository.RegistroTempoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock private RegistroTempoRepository registroTempoRepository;
    @Mock private EdicaoService edicaoService;
    @InjectMocks private RankingService rankingService;

    private Edicao buildEdicao(Long id) {
        Evento evento = new Evento();
        evento.setId(1L);
        evento.setNome("Descida da Ladeira");
        evento.setEdicoes(new ArrayList<>());

        Edicao e = new Edicao();
        e.setId(id);
        e.setEvento(evento);
        e.setAno(2025);
        e.setStatus(StatusEdicao.EM_ANDAMENTO);
        e.setEquipes(new ArrayList<>());
        e.setBaterias(new ArrayList<>());
        return e;
    }

    private Equipe buildEquipe(Long id, String nome) {
        Equipe eq = new Equipe();
        eq.setId(id);
        eq.setNome(nome);
        eq.setCurso("ADS");
        eq.setStatusInscricao(StatusEquipe.APROVADA);
        eq.setMembros(new ArrayList<>());
        eq.setRegistros(new ArrayList<>());
        return eq;
    }

    private RegistroTempo buildRegistro(Equipe equipe, double tempo) {
        RegistroTempo r = new RegistroTempo();
        r.setEquipe(equipe);
        r.setTempoMilissegundos(tempo);
        r.setTipoRegistro(TipoRegistro.CHEGADA);
        r.setValidado(true);
        return r;
    }

    @Test
    void deveRetornarRankingVazioSemRegistros() {
        when(edicaoService.buscarEntidade(1L)).thenReturn(buildEdicao(1L));
        when(registroTempoRepository.findTemposValidadosPorEdicao(1L)).thenReturn(List.of());

        RankingResponse response = rankingService.calcularRanking(1L);

        assertThat(response.edicaoId()).isEqualTo(1L);
        assertThat(response.nomeEvento()).isEqualTo("Descida da Ladeira");
        assertThat(response.classificacao()).isEmpty();
    }

    @Test
    void deveCalcularRankingComUmaEquipe() {
        Equipe equipe = buildEquipe(1L, "Team Fatec");
        when(edicaoService.buscarEntidade(1L)).thenReturn(buildEdicao(1L));
        when(registroTempoRepository.findTemposValidadosPorEdicao(1L)).thenReturn(List.of(
                buildRegistro(equipe, 15000.0),
                buildRegistro(equipe, 14500.0)
        ));

        RankingResponse response = rankingService.calcularRanking(1L);

        assertThat(response.classificacao()).hasSize(1);
        assertThat(response.classificacao().get(0).posicao()).isEqualTo(1);
        assertThat(response.classificacao().get(0).melhorTempo()).isEqualTo(14500.0);
        assertThat(response.classificacao().get(0).totalDescidas()).isEqualTo(2);
    }

    @Test
    void deveOrdenarPorMelhorTempoAscendente() {
        Equipe equipeA = buildEquipe(1L, "Team Alpha");
        Equipe equipeB = buildEquipe(2L, "Team Beta");

        when(edicaoService.buscarEntidade(1L)).thenReturn(buildEdicao(1L));
        when(registroTempoRepository.findTemposValidadosPorEdicao(1L)).thenReturn(List.of(
                buildRegistro(equipeA, 16000.0),  // mais lenta
                buildRegistro(equipeB, 13500.0)   // mais rápida
        ));

        RankingResponse response = rankingService.calcularRanking(1L);

        assertThat(response.classificacao()).hasSize(2);
        assertThat(response.classificacao().get(0).equipeNome()).isEqualTo("Team Beta");
        assertThat(response.classificacao().get(0).posicao()).isEqualTo(1);
        assertThat(response.classificacao().get(1).equipeNome()).isEqualTo("Team Alpha");
        assertThat(response.classificacao().get(1).posicao()).isEqualTo(2);
    }

    @Test
    void deveCalcularMediaCorretamente() {
        Equipe equipe = buildEquipe(1L, "Team Fatec");
        when(edicaoService.buscarEntidade(1L)).thenReturn(buildEdicao(1L));
        when(registroTempoRepository.findTemposValidadosPorEdicao(1L)).thenReturn(List.of(
                buildRegistro(equipe, 10000.0),
                buildRegistro(equipe, 12000.0),
                buildRegistro(equipe, 14000.0)
        ));

        RankingResponse response = rankingService.calcularRanking(1L);

        assertThat(response.classificacao().get(0).mediaTempo()).isEqualTo(12000.0);
        assertThat(response.classificacao().get(0).melhorTempo()).isEqualTo(10000.0);
    }
}
