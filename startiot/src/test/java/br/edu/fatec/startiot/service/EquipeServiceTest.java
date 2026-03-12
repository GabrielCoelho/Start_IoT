package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import br.edu.fatec.startiot.dto.request.EquipeRequest;
import br.edu.fatec.startiot.dto.response.EquipeResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.EquipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    @Mock private EquipeRepository equipeRepository;
    @Mock private EdicaoService edicaoService;
    @InjectMocks private EquipeService equipeService;

    private Edicao buildEdicao(StatusEdicao status) {
        Evento evento = new Evento();
        evento.setId(1L);
        evento.setNome("Descida");
        evento.setEdicoes(new ArrayList<>());

        Edicao e = new Edicao();
        e.setId(10L);
        e.setEvento(evento);
        e.setAno(2025);
        e.setStatus(status);
        e.setEquipes(new ArrayList<>());
        e.setBaterias(new ArrayList<>());
        return e;
    }

    private Equipe buildEquipe(Long id, StatusEquipe status) {
        Equipe eq = new Equipe();
        eq.setId(id);
        eq.setEdicao(buildEdicao(StatusEdicao.PLANEJADA));
        eq.setNome("Team Fatec");
        eq.setCurso("ADS");
        eq.setStatusInscricao(status);
        eq.setMembros(new ArrayList<>());
        eq.setRegistros(new ArrayList<>());
        return eq;
    }

    @Test
    void deveInscreverEquipe() {
        Edicao edicao = buildEdicao(StatusEdicao.PLANEJADA);
        Equipe salva = buildEquipe(1L, StatusEquipe.PENDENTE);

        when(edicaoService.buscarEntidade(10L)).thenReturn(edicao);
        when(equipeRepository.existsByEdicaoIdAndNome(10L, "Team Fatec")).thenReturn(false);
        when(equipeRepository.save(any())).thenReturn(salva);

        EquipeResponse response = equipeService.inscrever(new EquipeRequest(10L, "Team Fatec", "ADS", "A"));

        assertThat(response.statusInscricao()).isEqualTo(StatusEquipe.PENDENTE);
    }

    @Test
    void deveLancarBusinessSeEdicaoNaoPlanejada() {
        when(edicaoService.buscarEntidade(10L)).thenReturn(buildEdicao(StatusEdicao.EM_ANDAMENTO));

        assertThatThrownBy(() -> equipeService.inscrever(new EquipeRequest(10L, "Team X", "ADS", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_ANDAMENTO");
    }

    @Test
    void deveLancarConflictSeNomeJaExisteNaEdicao() {
        when(edicaoService.buscarEntidade(10L)).thenReturn(buildEdicao(StatusEdicao.PLANEJADA));
        when(equipeRepository.existsByEdicaoIdAndNome(10L, "Team Fatec")).thenReturn(true);

        assertThatThrownBy(() -> equipeService.inscrever(new EquipeRequest(10L, "Team Fatec", "ADS", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Team Fatec");
    }

    @Test
    void deveBuscarPorId() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(buildEquipe(1L, StatusEquipe.PENDENTE)));

        EquipeResponse response = equipeService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(equipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipeService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveAprovarEquipePendente() {
        Equipe equipe = buildEquipe(1L, StatusEquipe.PENDENTE);
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(equipeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EquipeResponse response = equipeService.aprovar(1L);

        assertThat(response.statusInscricao()).isEqualTo(StatusEquipe.APROVADA);
    }

    @Test
    void deveLancarBusinessAoAprovarEquipeNaoPendente() {
        Equipe equipe = buildEquipe(1L, StatusEquipe.APROVADA);
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));

        assertThatThrownBy(() -> equipeService.aprovar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APROVADA");
    }

    @Test
    void deveReprovarEquipePendente() {
        Equipe equipe = buildEquipe(1L, StatusEquipe.PENDENTE);
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(equipeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EquipeResponse response = equipeService.reprovar(1L);

        assertThat(response.statusInscricao()).isEqualTo(StatusEquipe.REPROVADA);
    }

    @Test
    void deveCancelarEquipe() {
        Equipe equipe = buildEquipe(1L, StatusEquipe.APROVADA);
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(equipeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EquipeResponse response = equipeService.cancelar(1L);

        assertThat(response.statusInscricao()).isEqualTo(StatusEquipe.CANCELADA);
    }

    @Test
    void deveLancarBusinessAoCancelarEquipeJaCancelada() {
        Equipe equipe = buildEquipe(1L, StatusEquipe.CANCELADA);
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));

        assertThatThrownBy(() -> equipeService.cancelar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelada");
    }
}
