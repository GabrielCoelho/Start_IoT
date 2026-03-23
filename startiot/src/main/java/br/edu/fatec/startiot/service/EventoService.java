package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.dto.request.EventoRequest;
import br.edu.fatec.startiot.dto.response.EventoResponse;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Transactional
    public EventoResponse criar(EventoRequest request) {
        if (eventoRepository.existsByNome(request.nome())) {
            throw new ConflictException("Evento com nome '%s' já existe".formatted(request.nome()));
        }

        Evento evento = new Evento();
        evento.setNome(request.nome());
        evento.setDescricao(request.descricao());

        return toResponse(eventoRepository.save(evento));
    }

    @Transactional(readOnly = true)
    public EventoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public EventoResponse atualizar(Long id, EventoRequest request) {
        Evento evento = buscarEntidade(id);

        boolean nomeAlterado = !evento.getNome().equals(request.nome());
        if (nomeAlterado && eventoRepository.existsByNome(request.nome())) {
            throw new ConflictException("Evento com nome '%s' já existe".formatted(request.nome()));
        }

        evento.setNome(request.nome());
        evento.setDescricao(request.descricao());

        return toResponse(eventoRepository.save(evento));
    }

    public Evento buscarEntidade(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Evento", id));
    }

    private EventoResponse toResponse(Evento e) {
        return new EventoResponse(
                e.getId(),
                e.getNome(),
                e.getDescricao(),
                e.getEdicoes().size(),
                e.getDataCriacao(),
                e.getDataAtualizacao()
        );
    }
}
