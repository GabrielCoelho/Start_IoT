package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.AlocacaoEquipeCorrida;
import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Corrida;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import br.edu.fatec.startiot.domain.enums.StatusCorrida;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import br.edu.fatec.startiot.dto.request.AlocacaoEquipeCorridaRequest;
import br.edu.fatec.startiot.dto.request.CorridaRequest;
import br.edu.fatec.startiot.dto.response.AlocacaoEquipeCorridaResponse;
import br.edu.fatec.startiot.dto.response.CorridaResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.AlocacaoEquipeCorridaRepository;
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
    private final AlocacaoEquipeCorridaRepository alocacaoRepository;
    private final BateriaService bateriaService;
    private final EquipeService equipeService;

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

    /**
     * Pré-aloca uma equipe em uma corrida antes de seu início.
     *
     * <p>A alocação define quais equipes participarão da corrida. Uma vez que existam
     * alocações, apenas as equipes alocadas poderão ter tempos registrados via
     * {@code /api/registros-tempo}. Corridas sem nenhuma alocação continuam aceitando
     * qualquer equipe aprovada (retrocompatibilidade).
     *
     * @throws BusinessException se a corrida não estiver AGUARDANDO
     * @throws BusinessException se a equipe não pertencer à edição da corrida ou não estiver APROVADA
     * @throws ConflictException se a equipe já estiver alocada nesta corrida
     */
    @Transactional
    public AlocacaoEquipeCorridaResponse alocarEquipe(Long corridaId, AlocacaoEquipeCorridaRequest request) {
        Corrida corrida = buscarEntidade(corridaId);

        if (corrida.getStatus() != StatusCorrida.AGUARDANDO) {
            throw new BusinessException(
                    "Não é possível alocar equipes em uma corrida com status '%s'. " +
                    "A alocação deve ser feita antes de iniciar a corrida.".formatted(corrida.getStatus())
            );
        }

        Equipe equipe = equipeService.buscarEntidade(request.equipeId());

        // A equipe deve pertencer à mesma edição da corrida
        Long edicaoCorrente = corrida.getBateria().getEdicao().getId();
        if (!equipe.getEdicao().getId().equals(edicaoCorrente)) {
            throw new BusinessException(
                    "Equipe '%s' não pertence à edição desta corrida".formatted(equipe.getNome())
            );
        }

        if (equipe.getStatusInscricao() != StatusEquipe.APROVADA) {
            throw new BusinessException(
                    "Equipe '%s' não pode ser alocada pois está com status '%s'. " +
                    "Apenas equipes APROVADAS podem participar de corridas."
                    .formatted(equipe.getNome(), equipe.getStatusInscricao())
            );
        }

        if (alocacaoRepository.existsByCorridaIdAndEquipeId(corridaId, equipe.getId())) {
            throw new ConflictException(
                    "Equipe '%s' já está alocada nesta corrida".formatted(equipe.getNome())
            );
        }

        AlocacaoEquipeCorrida alocacao = new AlocacaoEquipeCorrida();
        alocacao.setCorrida(corrida);
        alocacao.setEquipe(equipe);

        return toAlocacaoResponse(alocacaoRepository.save(alocacao));
    }

    /**
     * Remove a pré-alocação de uma equipe em uma corrida.
     *
     * <p>Só é possível remover alocações enquanto a corrida estiver AGUARDANDO.
     * Após iniciada, a lista de equipes torna-se imutável.
     *
     * @throws BusinessException  se a corrida não estiver AGUARDANDO
     * @throws NotFoundException  se a equipe não estiver alocada nesta corrida
     */
    @Transactional
    public void removerAlocacao(Long corridaId, Long equipeId) {
        Corrida corrida = buscarEntidade(corridaId);

        if (corrida.getStatus() != StatusCorrida.AGUARDANDO) {
            throw new BusinessException(
                    "Não é possível remover alocações de uma corrida com status '%s'".formatted(corrida.getStatus())
            );
        }

        AlocacaoEquipeCorrida alocacao = alocacaoRepository
                .findByCorridaIdAndEquipeId(corridaId, equipeId)
                .orElseThrow(() -> new NotFoundException(
                        "Equipe de id %d não está alocada na corrida de id %d".formatted(equipeId, corridaId)
                ));

        alocacaoRepository.delete(alocacao);
    }

    /**
     * Lista todas as equipes pré-alocadas em uma corrida, ordenadas por data de alocação.
     *
     * <p>Use este endpoint no frontend para carregar apenas as equipes participantes
     * na tela de cronometragem, evitando exibir a lista completa de equipes da edição.
     */
    @Transactional(readOnly = true)
    public List<AlocacaoEquipeCorridaResponse> listarAlocacoes(Long corridaId) {
        buscarEntidade(corridaId);
        return alocacaoRepository
                .findByCorridaIdOrderByDataCriacao(corridaId)
                .stream()
                .map(this::toAlocacaoResponse)
                .toList();
    }

    public Corrida buscarEntidade(Long id) {
        return corridaRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Corrida", id));
    }

    private AlocacaoEquipeCorridaResponse toAlocacaoResponse(AlocacaoEquipeCorrida a) {
        return new AlocacaoEquipeCorridaResponse(
                a.getId(),
                a.getCorrida().getId(),
                a.getCorrida().getOrdem(),
                a.getEquipe().getId(),
                a.getEquipe().getNome(),
                a.getEquipe().getCurso(),
                a.getDataCriacao(),
                a.getDataAtualizacao()
        );
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
