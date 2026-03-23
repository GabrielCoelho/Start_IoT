package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Corrida;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import br.edu.fatec.startiot.domain.enums.StatusCorrida;
import br.edu.fatec.startiot.dto.request.CorridaRequest;
import br.edu.fatec.startiot.dto.response.CorridaResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.CorridaRepository;
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
class CorridaServiceTest {

    @Mock private CorridaRepository corridaRepository;
    @Mock private BateriaService bateriaService;
    @InjectMocks private CorridaService corridaService;

    private Bateria buildBateria(StatusBateria status) {
        Bateria b = new Bateria();
        b.setId(5L);
        b.setNumero(1);
        b.setStatus(status);
        b.setCorridas(new ArrayList<>());
        return b;
    }

    private Corrida buildCorrida(Long id, StatusCorrida status) {
        Corrida c = new Corrida();
        c.setId(id);
        c.setBateria(buildBateria(StatusBateria.EM_ANDAMENTO));
        c.setOrdem(1);
        c.setStatus(status);
        c.setRegistros(new ArrayList<>());
        return c;
    }

    @Test
    void deveCriarCorrida() {
        Bateria bateria = buildBateria(StatusBateria.AGUARDANDO);
        Corrida salva = buildCorrida(1L, StatusCorrida.AGUARDANDO);

        when(bateriaService.buscarEntidade(5L)).thenReturn(bateria);
        when(corridaRepository.existsByBateriaIdAndOrdem(5L, 1)).thenReturn(false);
        when(corridaRepository.save(any())).thenReturn(salva);

        CorridaResponse response = corridaService.criar(new CorridaRequest(5L, 1));

        assertThat(response.status()).isEqualTo(StatusCorrida.AGUARDANDO);
    }

    @Test
    void deveLancarConflictSeOrdemJaExisteNaBateria() {
        when(bateriaService.buscarEntidade(5L)).thenReturn(buildBateria(StatusBateria.AGUARDANDO));
        when(corridaRepository.existsByBateriaIdAndOrdem(5L, 1)).thenReturn(true);

        assertThatThrownBy(() -> corridaService.criar(new CorridaRequest(5L, 1)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("1");
    }

    @Test
    void deveBuscarPorId() {
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(buildCorrida(1L, StatusCorrida.AGUARDANDO)));

        CorridaResponse response = corridaService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(corridaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> corridaService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveIniciarCorridaAguardando() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.AGUARDANDO);
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));
        when(corridaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CorridaResponse response = corridaService.iniciar(1L);

        assertThat(response.status()).isEqualTo(StatusCorrida.EM_ANDAMENTO);
        assertThat(response.horarioInicio()).isNotNull();
    }

    @Test
    void deveLancarBusinessAoIniciarCorridaNaoAguardando() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.EM_ANDAMENTO);
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));

        assertThatThrownBy(() -> corridaService.iniciar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_ANDAMENTO");
    }

    @Test
    void deveLancarBusinessAoIniciarCorridaComBateriaNaoEmAndamento() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.AGUARDANDO);
        corrida.setBateria(buildBateria(StatusBateria.AGUARDANDO));
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));

        assertThatThrownBy(() -> corridaService.iniciar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_ANDAMENTO");
    }

    @Test
    void deveFinalizarCorridaEmAndamento() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.EM_ANDAMENTO);
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));
        when(corridaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CorridaResponse response = corridaService.finalizar(1L);

        assertThat(response.status()).isEqualTo(StatusCorrida.FINALIZADA);
        assertThat(response.horarioFim()).isNotNull();
    }

    @Test
    void deveCancelarCorrida() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.AGUARDANDO);
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));
        when(corridaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CorridaResponse response = corridaService.cancelar(1L);

        assertThat(response.status()).isEqualTo(StatusCorrida.CANCELADA);
    }

    @Test
    void deveLancarBusinessAoCancelarCorridaFinalizada() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.FINALIZADA);
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));

        assertThatThrownBy(() -> corridaService.cancelar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizada");
    }

    @Test
    void deveDesclassificarCorridaFinalizada() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.FINALIZADA);
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));
        when(corridaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CorridaResponse response = corridaService.desclassificar(1L);

        assertThat(response.status()).isEqualTo(StatusCorrida.DESCLASSIFICADA);
    }

    @Test
    void deveLancarBusinessAoDesclassificarCorridaAguardando() {
        Corrida corrida = buildCorrida(1L, StatusCorrida.AGUARDANDO);
        when(corridaRepository.findById(1L)).thenReturn(Optional.of(corrida));

        assertThatThrownBy(() -> corridaService.desclassificar(1L))
                .isInstanceOf(BusinessException.class);
    }
}
