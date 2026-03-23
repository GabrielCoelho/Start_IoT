package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.dto.request.BateriaRequest;
import br.edu.fatec.startiot.dto.response.BateriaResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.BateriaRepository;
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
class BateriaServiceTest {

    @Mock private BateriaRepository bateriaRepository;
    @Mock private EdicaoService edicaoService;
    @InjectMocks private BateriaService bateriaService;

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

    private Bateria buildBateria(Long id, StatusBateria status) {
        Bateria b = new Bateria();
        b.setId(id);
        b.setEdicao(buildEdicao(StatusEdicao.EM_ANDAMENTO));
        b.setNumero(1);
        b.setTipo("eliminatoria");
        b.setStatus(status);
        b.setCorridas(new ArrayList<>());
        return b;
    }

    @Test
    void deveCriarBateria() {
        Edicao edicao = buildEdicao(StatusEdicao.PLANEJADA);
        Bateria salva = buildBateria(1L, StatusBateria.AGUARDANDO);

        when(edicaoService.buscarEntidade(10L)).thenReturn(edicao);
        when(bateriaRepository.existsByEdicaoIdAndNumero(10L, 1)).thenReturn(false);
        when(bateriaRepository.save(any())).thenReturn(salva);

        BateriaResponse response = bateriaService.criar(new BateriaRequest(10L, 1, "eliminatoria", null));

        assertThat(response.status()).isEqualTo(StatusBateria.AGUARDANDO);
        assertThat(response.numero()).isEqualTo(1);
    }

    @Test
    void deveLancarConflictSeNumeroJaExisteNaEdicao() {
        when(edicaoService.buscarEntidade(10L)).thenReturn(buildEdicao(StatusEdicao.PLANEJADA));
        when(bateriaRepository.existsByEdicaoIdAndNumero(10L, 1)).thenReturn(true);

        assertThatThrownBy(() -> bateriaService.criar(new BateriaRequest(10L, 1, "elim", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("1");
    }

    @Test
    void deveBuscarPorId() {
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(buildBateria(1L, StatusBateria.AGUARDANDO)));

        BateriaResponse response = bateriaService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(bateriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bateriaService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveIniciarBateriaAguardando() {
        Bateria bateria = buildBateria(1L, StatusBateria.AGUARDANDO);
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(bateria));
        when(bateriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BateriaResponse response = bateriaService.iniciar(1L);

        assertThat(response.status()).isEqualTo(StatusBateria.EM_ANDAMENTO);
    }

    @Test
    void deveLancarBusinessAoIniciarBateriaNaoAguardando() {
        Bateria bateria = buildBateria(1L, StatusBateria.EM_ANDAMENTO);
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(bateria));

        assertThatThrownBy(() -> bateriaService.iniciar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_ANDAMENTO");
    }

    @Test
    void deveLancarBusinessAoIniciarComEdicaoNaoEmAndamento() {
        Bateria bateria = buildBateria(1L, StatusBateria.AGUARDANDO);
        bateria.setEdicao(buildEdicao(StatusEdicao.PLANEJADA));
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(bateria));

        assertThatThrownBy(() -> bateriaService.iniciar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_ANDAMENTO");
    }

    @Test
    void deveFinalizarBateriaEmAndamento() {
        Bateria bateria = buildBateria(1L, StatusBateria.EM_ANDAMENTO);
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(bateria));
        when(bateriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BateriaResponse response = bateriaService.finalizar(1L);

        assertThat(response.status()).isEqualTo(StatusBateria.FINALIZADA);
    }

    @Test
    void deveLancarBusinessAoFinalizarBateriaNaoEmAndamento() {
        Bateria bateria = buildBateria(1L, StatusBateria.AGUARDANDO);
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(bateria));

        assertThatThrownBy(() -> bateriaService.finalizar(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveCancelarBateriaAguardando() {
        Bateria bateria = buildBateria(1L, StatusBateria.AGUARDANDO);
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(bateria));
        when(bateriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BateriaResponse response = bateriaService.cancelar(1L);

        assertThat(response.status()).isEqualTo(StatusBateria.CANCELADA);
    }

    @Test
    void deveLancarBusinessAoCancelarBateriaFinalizada() {
        Bateria bateria = buildBateria(1L, StatusBateria.FINALIZADA);
        when(bateriaRepository.findById(1L)).thenReturn(Optional.of(bateria));

        assertThatThrownBy(() -> bateriaService.cancelar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizada");
    }
}
