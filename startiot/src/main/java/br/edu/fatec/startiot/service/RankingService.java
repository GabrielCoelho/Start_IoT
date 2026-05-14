package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.RegistroTempo;
import br.edu.fatec.startiot.dto.response.BateriaInfo;
import br.edu.fatec.startiot.dto.response.BateriaTempoItem;
import br.edu.fatec.startiot.dto.response.RankingItemResponse;
import br.edu.fatec.startiot.dto.response.RankingResponse;
import br.edu.fatec.startiot.repository.RegistroTempoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RegistroTempoRepository registroTempoRepository;
    private final EdicaoService edicaoService;

    @Transactional(readOnly = true)
    public RankingResponse calcularRanking(Long edicaoId) {
        Edicao edicao = edicaoService.buscarEntidade(edicaoId);

        // Todos os tempos de chegada validados de corridas FINALIZADAS nesta edição
        List<RegistroTempo> tempos = registroTempoRepository.findTemposValidadosPorEdicao(edicaoId);

        // Baterias únicas que têm pelo menos um tempo registrado, ordenadas por número
        List<Bateria> baterias = tempos.stream()
                .map(rt -> rt.getCorrida().getBateria())
                .distinct()
                .sorted(Comparator.comparingInt(Bateria::getNumero))
                .toList();

        List<BateriaInfo> bateriaInfos = baterias.stream()
                .map(b -> new BateriaInfo(b.getId(), b.getNumero(), b.getTipo()))
                .toList();

        // Agrupar registros por equipe
        Map<Equipe, List<RegistroTempo>> porEquipe = tempos.stream()
                .collect(Collectors.groupingBy(RegistroTempo::getEquipe));

        AtomicInteger posicao = new AtomicInteger(1);

        List<RankingItemResponse> classificacao = porEquipe.entrySet().stream()
                .map(entry -> calcularItem(entry.getKey(), entry.getValue(), baterias))
                .sorted(Comparator.comparingDouble(RankingItemResponse::melhorTempo))
                .map(item -> new RankingItemResponse(
                        posicao.getAndIncrement(),
                        item.equipeId(),
                        item.equipeNome(),
                        item.equipeCurso(),
                        item.totalDescidas(),
                        item.melhorTempo(),
                        item.mediaTempo(),
                        item.porBateria()
                ))
                .toList();

        return new RankingResponse(
                edicao.getId(),
                edicao.getAno(),
                edicao.getEvento().getNome(),
                bateriaInfos,
                classificacao
        );
    }

    private RankingItemResponse calcularItem(Equipe equipe, List<RegistroTempo> tempos, List<Bateria> todasBaterias) {
        double melhor = tempos.stream()
                .mapToDouble(RegistroTempo::getTempoMilissegundos)
                .min()
                .orElse(0.0);

        double media = tempos.stream()
                .mapToDouble(RegistroTempo::getTempoMilissegundos)
                .average()
                .orElse(0.0);

        // Agrupar os tempos desta equipe por bateria (usando bateriaId como chave)
        Map<Long, List<RegistroTempo>> porBateriaId = tempos.stream()
                .collect(Collectors.groupingBy(rt -> rt.getCorrida().getBateria().getId()));

        // Gerar um BateriaTempoItem para cada bateria em que a equipe participou
        List<BateriaTempoItem> porBateria = todasBaterias.stream()
                .filter(b -> porBateriaId.containsKey(b.getId()))
                .map(b -> {
                    List<RegistroTempo> temposBateria = porBateriaId.get(b.getId());
                    double melhorBateria = temposBateria.stream()
                            .mapToDouble(RegistroTempo::getTempoMilissegundos)
                            .min()
                            .orElse(0.0);
                    return new BateriaTempoItem(
                            b.getId(),
                            b.getNumero(),
                            b.getTipo(),
                            melhorBateria,
                            temposBateria.size()
                    );
                })
                .toList();

        return new RankingItemResponse(
                0,
                equipe.getId(),
                equipe.getNome(),
                equipe.getCurso(),
                tempos.size(),
                melhor,
                media,
                porBateria
        );
    }
}
