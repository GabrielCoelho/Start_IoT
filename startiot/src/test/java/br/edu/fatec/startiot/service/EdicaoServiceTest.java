package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.dto.request.EdicaoRequest;
import br.edu.fatec.startiot.dto.response.EdicaoResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.EdicaoRepository;
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
class EdicaoServiceTest {

    @Mock private EdicaoRepository edicaoRepository;
    @Mock private EventoService eventoService;
    @InjectMocks private EdicaoService edicaoService;

    private Evento buildEvento(Long id) {
        Evento e = new Evento();
        e.setId(id);
        e.setNome("Descida da Ladeira");
        e.setEdicoes(new ArrayList<>());
        return e;
    }

    private Edicao buildEdicao(Long id, StatusEdicao status) {
        Evento evento = buildEvento(10L);
        Edicao e = new Edicao();
        e.setId(id);
        e.setEvento(evento);
        e.setAno(2025);
        e.setStatus(status);
        e.setEquipes(new ArrayList<>());
        e.setBaterias(new ArrayList<>());
        return e;
    }

    @Test
    void deveCriarEdicao() {
        Evento evento = buildEvento(10L);
        Edicao salva = buildEdicao(1L, StatusEdicao.PLANEJADA);

        when(eventoService.buscarEntidade(10L)).thenReturn(evento);
        when(edicaoRepository.save(any())).thenReturn(salva);

        EdicaoResponse response = edicaoService.criar(new EdicaoRequest(10L, 2025, null, null, StatusEdicao.PLANEJADA));

        assertThat(response.ano()).isEqualTo(2025);
        assertThat(response.status()).isEqualTo(StatusEdicao.PLANEJADA);
    }

    @Test
    void deveCriarDuasEdicoesNoMesmoAno() {
        Evento evento = buildEvento(10L);
        Edicao salva = buildEdicao(1L, StatusEdicao.PLANEJADA);

        when(eventoService.buscarEntidade(10L)).thenReturn(evento);
        when(edicaoRepository.save(any())).thenReturn(salva);

        edicaoService.criar(new EdicaoRequest(10L, 2025, 11, null, StatusEdicao.PLANEJADA));
        edicaoService.criar(new EdicaoRequest(10L, 2025, 12, null, StatusEdicao.PLANEJADA));
    }

    @Test
    void deveBuscarPorId() {
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(buildEdicao(1L, StatusEdicao.PLANEJADA)));

        EdicaoResponse response = edicaoService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(edicaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> edicaoService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveTransicionarDePlanejedaParaEmAndamento() {
        Edicao edicao = buildEdicao(1L, StatusEdicao.PLANEJADA);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(edicao));
        when(edicaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EdicaoResponse response = edicaoService.atualizarStatus(1L, StatusEdicao.EM_ANDAMENTO);

        assertThat(response.status()).isEqualTo(StatusEdicao.EM_ANDAMENTO);
    }

    @Test
    void deveTransicionarDeEmAndamentoParaFinalizada() {
        Edicao edicao = buildEdicao(1L, StatusEdicao.EM_ANDAMENTO);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(edicao));
        when(edicaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EdicaoResponse response = edicaoService.atualizarStatus(1L, StatusEdicao.FINALIZADA);

        assertThat(response.status()).isEqualTo(StatusEdicao.FINALIZADA);
    }

    @Test
    void deveLancarBusinessParaTransicaoInvalida_finalizadaParaEmAndamento() {
        Edicao edicao = buildEdicao(1L, StatusEdicao.FINALIZADA);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(edicao));

        assertThatThrownBy(() -> edicaoService.atualizarStatus(1L, StatusEdicao.EM_ANDAMENTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FINALIZADA");
    }

    @Test
    void deveLancarBusinessParaTransicaoInvalida_planejedaParaFinalizada() {
        Edicao edicao = buildEdicao(1L, StatusEdicao.PLANEJADA);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(edicao));

        assertThatThrownBy(() -> edicaoService.atualizarStatus(1L, StatusEdicao.FINALIZADA))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveCancelarAPartirDePlanejada() {
        Edicao edicao = buildEdicao(1L, StatusEdicao.PLANEJADA);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(edicao));
        when(edicaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EdicaoResponse response = edicaoService.atualizarStatus(1L, StatusEdicao.CANCELADA);

        assertThat(response.status()).isEqualTo(StatusEdicao.CANCELADA);
    }
}
