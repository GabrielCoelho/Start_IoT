package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.BateriaEliminacao;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.RegistroTempo;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import br.edu.fatec.startiot.dto.request.BateriaRequest;
import br.edu.fatec.startiot.dto.request.FinalizarBateriaRequest;
import br.edu.fatec.startiot.dto.response.BateriaResponse;
import br.edu.fatec.startiot.dto.response.EquipeResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.BateriaEliminacaoRepository;
import br.edu.fatec.startiot.repository.BateriaRepository;
import br.edu.fatec.startiot.repository.EquipeRepository;
import br.edu.fatec.startiot.repository.RegistroTempoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BateriaService {

    private final BateriaRepository bateriaRepository;
    private final EdicaoService edicaoService;
    private final RegistroTempoRepository registroTempoRepository;
    private final BateriaEliminacaoRepository eliminacaoRepository;
    private final EquipeRepository equipeRepository;

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
    public BateriaResponse finalizar(Long id, FinalizarBateriaRequest request) {
        Bateria bateria = buscarEntidade(id);

        if (bateria.getStatus() != StatusBateria.EM_ANDAMENTO) {
            throw new BusinessException(
                    "Bateria não pode ser finalizada com status '%s'".formatted(bateria.getStatus())
            );
        }

        if (request != null && request.posicaoCorte() != null && request.posicaoCorte() > 0) {
            aplicarCorte(bateria, request.posicaoCorte());
        }

        bateria.setStatus(StatusBateria.FINALIZADA);
        return toResponse(bateriaRepository.save(bateria));
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> listarEquipesDisponiveis(Long bateriaId) {
        Bateria bateria = buscarEntidade(bateriaId);
        Long edicaoId = bateria.getEdicao().getId();

        Set<Long> eliminadas = eliminacaoRepository.findEquipeIdsEliminadasPorEdicao(edicaoId);

        return equipeRepository.findByEdicaoId(edicaoId).stream()
                .filter(e -> e.getStatusInscricao() == StatusEquipe.APROVADA)
                .filter(e -> !eliminadas.contains(e.getId()))
                .map(this::toEquipeResponse)
                .toList();
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

    private void aplicarCorte(Bateria bateria, int posicaoCorte) {
        List<RegistroTempo> tempos = registroTempoRepository.findChegadasPorBateria(bateria.getId());

        // Melhor tempo por equipe
        Map<Long, Double> melhorPorEquipe = tempos.stream()
                .collect(Collectors.groupingBy(
                        rt -> rt.getEquipe().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparingDouble(RegistroTempo::getTempoEfetivo)),
                                opt -> opt.map(RegistroTempo::getTempoEfetivo).orElse(Double.MAX_VALUE)
                        )
                ));

        Map<Long, Equipe> equipeMap = tempos.stream()
                .collect(Collectors.toMap(rt -> rt.getEquipe().getId(), RegistroTempo::getEquipe, (a, b) -> a));

        List<Map.Entry<Long, Double>> ranking = melhorPorEquipe.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toList();

        // Equipes além do corte são eliminadas
        for (int i = posicaoCorte; i < ranking.size(); i++) {
            Long equipeId = ranking.get(i).getKey();
            Equipe equipe = equipeMap.get(equipeId);

            BateriaEliminacao elim = new BateriaEliminacao();
            elim.setBateria(bateria);
            elim.setEquipe(equipe);
            elim.setPosicao(i + 1);
            eliminacaoRepository.save(elim);
        }
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

    private EquipeResponse toEquipeResponse(Equipe e) {
        Boolean carrinhoAprovado = e.getCarrinho() != null ? e.getCarrinho().getAprovadoVistoria() : null;
        return new EquipeResponse(
                e.getId(),
                e.getEdicao().getId(),
                e.getNome(),
                e.getCurso(),
                e.getCategoria(),
                e.getStatusInscricao(),
                e.getMembros() != null ? e.getMembros().size() : 0,
                carrinhoAprovado,
                e.getDataCriacao(),
                e.getDataAtualizacao()
        );
    }
}
