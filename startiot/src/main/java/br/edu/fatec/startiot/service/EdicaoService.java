package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Evento;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.dto.request.EdicaoRequest;
import br.edu.fatec.startiot.dto.response.EdicaoResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.EdicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EdicaoService {

    private final EdicaoRepository edicaoRepository;
    private final EventoService eventoService;

    @Transactional
    public EdicaoResponse criar(EdicaoRequest request) {
        Evento evento = eventoService.buscarEntidade(request.eventoId());

        Edicao edicao = new Edicao();
        edicao.setEvento(evento);
        edicao.setAno(request.ano());
        edicao.setNumero(request.numero());
        edicao.setDataEvento(request.dataEvento());
        edicao.setStatus(request.status());

        return toResponse(edicaoRepository.save(edicao));
    }

    @Transactional(readOnly = true)
    public EdicaoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<EdicaoResponse> listarPorEvento(Long eventoId) {
        eventoService.buscarEntidade(eventoId);
        return edicaoRepository.findByEventoId(eventoId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EdicaoResponse> listarPorStatus(StatusEdicao status) {
        return edicaoRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public EdicaoResponse atualizarStatus(Long id, StatusEdicao novoStatus) {
        Edicao edicao = buscarEntidade(id);
        validarTransicaoStatus(edicao.getStatus(), novoStatus);
        edicao.setStatus(novoStatus);
        return toResponse(edicaoRepository.save(edicao));
    }

    public Edicao buscarEntidade(Long id) {
        return edicaoRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Edição", id));
    }

    private void validarTransicaoStatus(StatusEdicao atual, StatusEdicao novo) {
        boolean valida = switch (atual) {
            case PLANEJADA     -> novo == StatusEdicao.EM_ANDAMENTO || novo == StatusEdicao.CANCELADA;
            case EM_ANDAMENTO  -> novo == StatusEdicao.FINALIZADA   || novo == StatusEdicao.CANCELADA;
            case FINALIZADA, CANCELADA -> false;
        };

        if (!valida) {
            throw new BusinessException(
                    "Transição de status inválida: %s → %s".formatted(atual, novo)
            );
        }
    }

    private EdicaoResponse toResponse(Edicao e) {
        return new EdicaoResponse(
                e.getId(),
                e.getEvento().getId(),
                e.getEvento().getNome(),
                e.getAno(),
                e.getNumero(),
                e.getDataEvento(),
                e.getStatus(),
                e.getEquipes().size(),
                e.getBaterias().size(),
                e.getDataCriacao(),
                e.getDataAtualizacao()
        );
    }
}
