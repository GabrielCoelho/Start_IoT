package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.dto.request.BateriaRequest;
import br.edu.fatec.startiot.dto.response.BateriaResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.BateriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BateriaService {

    private final BateriaRepository bateriaRepository;
    private final EdicaoService edicaoService;

    @Transactional
    public BateriaResponse criar(BateriaRequest request) {
        Edicao edicao = edicaoService.buscarEntidade(request.edicaoId());

        if (bateriaRepository.existsByEdicaoIdAndNumero(request.edicaoId(), request.numero())) {
            throw new ConflictException(
                    "Já existe a bateria número %d nesta edição".formatted(request.numero())
            );
        }

        Bateria bateria = new Bateria();
        bateria.setEdicao(edicao);
        bateria.setNumero(request.numero());
        bateria.setTipo(request.tipo());
        bateria.setHorarioPrevisto(request.horarioPrevisto());
        bateria.setStatus(StatusBateria.AGUARDANDO);

        return toResponse(bateriaRepository.save(bateria));
    }

    @Transactional(readOnly = true)
    public BateriaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<BateriaResponse> listarPorEdicao(Long edicaoId) {
        edicaoService.buscarEntidade(edicaoId);
        return bateriaRepository.findByEdicaoIdOrderByNumero(edicaoId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BateriaResponse iniciar(Long id) {
        Bateria bateria = buscarEntidade(id);

        if (bateria.getStatus() != StatusBateria.AGUARDANDO) {
            throw new BusinessException(
                    "Bateria não pode ser iniciada com status '%s'".formatted(bateria.getStatus())
            );
        }

        if (bateria.getEdicao().getStatus() != StatusEdicao.EM_ANDAMENTO) {
            throw new BusinessException("A edição precisa estar EM_ANDAMENTO para iniciar uma bateria");
        }

        bateria.setStatus(StatusBateria.EM_ANDAMENTO);
        return toResponse(bateriaRepository.save(bateria));
    }

    @Transactional
    public BateriaResponse finalizar(Long id) {
        Bateria bateria = buscarEntidade(id);

        if (bateria.getStatus() != StatusBateria.EM_ANDAMENTO) {
            throw new BusinessException(
                    "Bateria não pode ser finalizada com status '%s'".formatted(bateria.getStatus())
            );
        }

        bateria.setStatus(StatusBateria.FINALIZADA);
        return toResponse(bateriaRepository.save(bateria));
    }

    @Transactional
    public BateriaResponse cancelar(Long id) {
        Bateria bateria = buscarEntidade(id);

        if (bateria.getStatus() == StatusBateria.FINALIZADA) {
            throw new BusinessException("Não é possível cancelar uma bateria já finalizada");
        }

        bateria.setStatus(StatusBateria.CANCELADA);
        return toResponse(bateriaRepository.save(bateria));
    }

    public Bateria buscarEntidade(Long id) {
        return bateriaRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Bateria", id));
    }

    private BateriaResponse toResponse(Bateria b) {
        return new BateriaResponse(
                b.getId(),
                b.getEdicao().getId(),
                b.getNumero(),
                b.getTipo(),
                b.getHorarioPrevisto(),
                b.getStatus(),
                b.getCorridas().size(),
                b.getDataCriacao(),
                b.getDataAtualizacao()
        );
    }
}
