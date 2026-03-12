package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.dto.request.EventoRequest;
import br.edu.fatec.startiot.dto.response.EventoResponse;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.EventoRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @InjectMocks private EventoService eventoService;

    private Evento buildEvento(Long id, String nome) {
        Evento e = new Evento();
        e.setId(id);
        e.setNome(nome);
        e.setDescricao("Evento de teste");
        e.setEdicoes(new ArrayList<>());
        return e;
    }

    @Test
    void deveCriarEvento() {
        var request = new EventoRequest("Descida da Ladeira", "Evento anual");
        Evento salvo = buildEvento(1L, "Descida da Ladeira");

        when(eventoRepository.existsByNome("Descida da Ladeira")).thenReturn(false);
        when(eventoRepository.save(any())).thenReturn(salvo);

        EventoResponse response = eventoService.criar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Descida da Ladeira");
        assertThat(response.totalEdicoes()).isZero();
    }

    @Test
    void deveLancarConflictSeNomeJaExiste() {
        when(eventoRepository.existsByNome("Descida da Ladeira")).thenReturn(true);

        assertThatThrownBy(() -> eventoService.criar(new EventoRequest("Descida da Ladeira", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Descida da Ladeira");
    }

    @Test
    void deveBuscarPorId() {
        Evento evento = buildEvento(1L, "Evento X");
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        EventoResponse response = eventoService.buscarPorId(1L);

        assertThat(response.nome()).isEqualTo("Evento X");
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveListarEventos() {
        when(eventoRepository.findAll()).thenReturn(List.of(
                buildEvento(1L, "Evento A"),
                buildEvento(2L, "Evento B")
        ));

        assertThat(eventoService.listar()).hasSize(2);
    }

    @Test
    void deveAtualizarEvento() {
        Evento existente = buildEvento(1L, "Nome Antigo");
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(eventoRepository.existsByNome("Nome Novo")).thenReturn(false);
        when(eventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EventoResponse response = eventoService.atualizar(1L, new EventoRequest("Nome Novo", "desc"));

        assertThat(response.nome()).isEqualTo("Nome Novo");
    }

    @Test
    void deveAtualizarSemConflictSeNomeNaoMudou() {
        Evento existente = buildEvento(1L, "Mesmo Nome");
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(eventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // não deve chamar existsByNome quando o nome não mudou
        EventoResponse response = eventoService.atualizar(1L, new EventoRequest("Mesmo Nome", "nova desc"));

        assertThat(response.nome()).isEqualTo("Mesmo Nome");
        verify(eventoRepository, never()).existsByNome(any());
    }
}
