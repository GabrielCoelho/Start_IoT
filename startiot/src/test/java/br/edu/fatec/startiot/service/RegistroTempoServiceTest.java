package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.*;
import br.edu.fatec.startiot.domain.enums.*;
import br.edu.fatec.startiot.dto.request.RegistroTempoRequest;
import br.edu.fatec.startiot.dto.response.RegistroTempoResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.RegistroTempoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroTempoServiceTest {

    @Mock private RegistroTempoRepository registroTempoRepository;
    @Mock private CorridaService corridaService;
    @Mock private EquipeService equipeService;
    @Mock private UsuarioService usuarioService;
    @InjectMocks private RegistroTempoService registroTempoService;

    private Corrida buildCorrida(StatusCorrida status) {
        Bateria bateria = new Bateria();
        bateria.setId(5L);
        bateria.setNumero(1);
        bateria.setStatus(StatusBateria.EM_ANDAMENTO);
        bateria.setCorridas(new ArrayList<>());

        Corrida c = new Corrida();
        c.setId(10L);
        c.setBateria(bateria);
        c.setOrdem(1);
        c.setStatus(status);
        c.setRegistros(new ArrayList<>());
        return c;
    }

    private Equipe buildEquipe(Long id) {
        Equipe eq = new Equipe();
        eq.setId(id);
        eq.setNome("Team Fatec");
        eq.setCurso("ADS");
        eq.setStatusInscricao(StatusEquipe.APROVADA);
        eq.setMembros(new ArrayList<>());
        eq.setRegistros(new ArrayList<>());
        return eq;
    }

    private Usuario buildUsuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Juiz 1");
        u.setEmail("juiz@fatec.br");
        u.setPerfil(PerfilUsuario.CRONOMETRISTA);
        u.setAtivo(true);
        u.setRegistros(new ArrayList<>());
        return u;
    }

    private RegistroTempo buildRegistro(Long id, boolean validado) {
        RegistroTempo r = new RegistroTempo();
        r.setId(id);
        r.setCorrida(buildCorrida(StatusCorrida.EM_ANDAMENTO));
        r.setEquipe(buildEquipe(1L));
        r.setUsuario(buildUsuario(1L));
        r.setTempoMilissegundos(15234.5);
        r.setTipoRegistro(TipoRegistro.CHEGADA);
        r.setValidado(validado);
        return r;
    }

    @Test
    void deveRegistrarTempo() {
        var request = new RegistroTempoRequest(10L, 1L, 15234.5, TipoRegistro.CHEGADA, null);
        RegistroTempo salvo = buildRegistro(1L, false);

        when(corridaService.buscarEntidade(10L)).thenReturn(buildCorrida(StatusCorrida.EM_ANDAMENTO));
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(usuarioService.buscarEntidade(99L)).thenReturn(buildUsuario(99L));
        when(registroTempoRepository.save(any())).thenReturn(salvo);

        RegistroTempoResponse response = registroTempoService.registrar(request, 99L);

        assertThat(response.validado()).isFalse();
        assertThat(response.tempoMilissegundos()).isEqualTo(15234.5);
    }

    @Test
    void deveLancarBusinessSeCorridaNaoEmAndamento() {
        var request = new RegistroTempoRequest(10L, 1L, 15234.5, TipoRegistro.CHEGADA, null);
        when(corridaService.buscarEntidade(10L)).thenReturn(buildCorrida(StatusCorrida.AGUARDANDO));

        assertThatThrownBy(() -> registroTempoService.registrar(request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AGUARDANDO");
    }

    @Test
    void deveBuscarPorId() {
        when(registroTempoRepository.findById(1L)).thenReturn(Optional.of(buildRegistro(1L, false)));

        RegistroTempoResponse response = registroTempoService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(registroTempoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registroTempoService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveListarPorCorrida() {
        when(corridaService.buscarEntidade(10L)).thenReturn(buildCorrida(StatusCorrida.EM_ANDAMENTO));
        when(registroTempoRepository.findByCorridaId(10L)).thenReturn(List.of(
                buildRegistro(1L, false), buildRegistro(2L, true)
        ));

        assertThat(registroTempoService.listarPorCorrida(10L)).hasSize(2);
    }

    @Test
    void deveValidarRegistro() {
        RegistroTempo registro = buildRegistro(1L, false);
        when(registroTempoRepository.findById(1L)).thenReturn(Optional.of(registro));
        when(registroTempoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroTempoResponse response = registroTempoService.validar(1L);

        assertThat(response.validado()).isTrue();
    }

    @Test
    void deveLancarBusinessAoValidarRegistroJaValidado() {
        RegistroTempo registro = buildRegistro(1L, true);
        when(registroTempoRepository.findById(1L)).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> registroTempoService.validar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("validado");
    }

    @Test
    void deveInvalidarRegistro() {
        RegistroTempo registro = buildRegistro(1L, true);
        when(registroTempoRepository.findById(1L)).thenReturn(Optional.of(registro));
        when(registroTempoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroTempoResponse response = registroTempoService.invalidar(1L);

        assertThat(response.validado()).isFalse();
    }

    @Test
    void deveLancarBusinessAoInvalidarRegistroJaInvalidado() {
        RegistroTempo registro = buildRegistro(1L, false);
        when(registroTempoRepository.findById(1L)).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> registroTempoService.invalidar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("invalidado");
    }
}
