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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorridaService {

    private final CorridaRepository corridaRepository;
    private final BateriaService bateriaService;

    @Transactional
    public CorridaResponse criar(CorridaRequest request) {
        Bateria bateria = bateriaService.buscarEntidade(request.bateriaId());

        if (corridaRepository.existsByBateriaIdAndOrdem(request.bateriaId(), request.ordem())) {
            throw new ConflictException(
                    "Já existe uma corrida com ordem %d nesta bateria".formatted(request.ordem())
            );
        }

        Corrida corrida = new Corrida();
        corrida.setBateria(bateria);
        corrida.setOrdem(request.ordem());
        corrida.setStatus(StatusCorrida.AGUARDANDO);

        return toResponse(corridaRepository.save(corrida));
    }

    @Transactional(readOnly = true)
    public CorridaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<CorridaResponse> listarPorBateria(Long bateriaId) {
        bateriaService.buscarEntidade(bateriaId);
        return corridaRepository.findByBateriaIdOrderByOrdem(bateriaId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CorridaResponse iniciar(Long id) {
        Corrida corrida = buscarEntidade(id);

        if (corrida.getStatus() != StatusCorrida.AGUARDANDO) {
            throw new BusinessException(
                    "Corrida não pode ser iniciada com status '%s'".formatted(corrida.getStatus())
            );
        }

        if (corrida.getBateria().getStatus() != StatusBateria.EM_ANDAMENTO) {
            throw new BusinessException("A bateria precisa estar EM_ANDAMENTO para iniciar uma corrida");
        }

        corrida.setStatus(StatusCorrida.EM_ANDAMENTO);
        corrida.setHorarioInicio(LocalDateTime.now());

        return toResponse(corridaRepository.save(corrida));
    }

    @Transactional
    public CorridaResponse finalizar(Long id) {
        Corrida corrida = buscarEntidade(id);

        if (corrida.getStatus() != StatusCorrida.EM_ANDAMENTO) {
            throw new BusinessException(
                    "Corrida não pode ser finalizada com status '%s'".formatted(corrida.getStatus())
            );
        }

        corrida.setStatus(StatusCorrida.FINALIZADA);
        corrida.setHorarioFim(LocalDateTime.now());

        return toResponse(corridaRepository.save(corrida));
    }

    @Transactional
    public CorridaResponse cancelar(Long id) {
        Corrida corrida = buscarEntidade(id);

        if (corrida.getStatus() == StatusCorrida.FINALIZADA) {
            throw new BusinessException("Não é possível cancelar uma corrida já finalizada");
        }

        corrida.setStatus(StatusCorrida.CANCELADA);
        corrida.setHorarioFim(LocalDateTime.now());

        return toResponse(corridaRepository.save(corrida));
    }

    @Transactional
    public CorridaResponse desclassificar(Long id) {
        Corrida corrida = buscarEntidade(id);

        if (corrida.getStatus() != StatusCorrida.FINALIZADA && corrida.getStatus() != StatusCorrida.EM_ANDAMENTO) {
            throw new BusinessException(
                    "Corrida não pode ser desclassificada com status '%s'".formatted(corrida.getStatus())
            );
        }

        corrida.setStatus(StatusCorrida.DESCLASSIFICADA);

        return toResponse(corridaRepository.save(corrida));
    }

    public Corrida buscarEntidade(Long id) {
        return corridaRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Corrida", id));
    }

    private CorridaResponse toResponse(Corrida c) {
        return new CorridaResponse(
                c.getId(),
                c.getBateria().getId(),
                c.getBateria().getNumero(),
                c.getOrdem(),
                c.getHorarioInicio(),
                c.getHorarioFim(),
                c.getStatus(),
                c.getRegistros().size(),
                c.getDataCriacao(),
                c.getDataAtualizacao()
        );
    }
}
