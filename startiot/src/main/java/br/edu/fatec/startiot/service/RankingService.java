package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.RegistroTempo;
import br.edu.fatec.startiot.dto.response.BateriaInfo;
import br.edu.fatec.startiot.dto.response.BateriaTempoItem;
import br.edu.fatec.startiot.dto.response.RankingItemResponse;
import br.edu.fatec.startiot.dto.response.RankingResponse;
import br.edu.fatec.startiot.repository.RegistroTempoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

        List<RegistroTempo> tempos = registroTempoRepository.findTemposValidadosPorEdicao(edicaoId);

        // Deduplicar baterias por ID (evita distinct() sobre entidades sem equals/hashCode)
        LinkedHashMap<Long, Bateria> bateriaMap = new LinkedHashMap<>();
        for (RegistroTempo rt : tempos) {
            Bateria b = rt.getCorrida().getBateria();
            bateriaMap.putIfAbsent(b.getId(), b);
        }
        List<Bateria> baterias = bateriaMap.values().stream()
                .sorted(Comparator.comparingInt(Bateria::getNumero))
                .toList();

        List<BateriaInfo> bateriaInfos = baterias.stream()
                .map(b -> new BateriaInfo(b.getId(), b.getNumero(), b.getTipo()))
                .toList();

        // Agrupar por equipeId (Long) para evitar mesmo problema com Equipe entity
        Map<Long, List<RegistroTempo>> porEquipeId = tempos.stream()
                .collect(Collectors.groupingBy(rt -> rt.getEquipe().getId()));

        AtomicInteger posicao = new AtomicInteger(1);

        List<RankingItemResponse> classificacao = porEquipeId.entrySet().stream()
                .map(entry -> {
                    List<RegistroTempo> registros = entry.getValue();
                    // Pega os dados da equipe do primeiro registro
                    var equipe = registros.get(0).getEquipe();
                    return calcularItem(equipe.getId(), equipe.getNome(), equipe.getCurso(), registros, baterias);
                })
                .sorted(Comparator.comparingDouble(RankingItemResponse::ultimoTempo))
                .map(item -> new RankingItemResponse(
                        posicao.getAndIncrement(),
                        item.equipeId(),
                        item.equipeNome(),
                        item.equipeCurso(),
                        item.totalDescidas(),
                        item.melhorTempo(),
                        item.ultimoTempo(),
                        item.tempoUltimaDescida(),
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

    private RankingItemResponse calcularItem(
            Long equipeId, String equipeNome, String equipeCurso,
            List<RegistroTempo> tempos, List<Bateria> todasBaterias) {

        double melhor = tempos.stream()
                .mapToDouble(RegistroTempo::getTempoEfetivo)
                .min().orElse(0.0);

        double media = tempos.stream()
                .mapToDouble(RegistroTempo::getTempoEfetivo)
                .average().orElse(0.0);

        // Agrupar por bateriaId
        Map<Long, List<RegistroTempo>> porBateriaId = tempos.stream()
                .collect(Collectors.groupingBy(rt -> rt.getCorrida().getBateria().getId()));

        List<BateriaTempoItem> porBateria = todasBaterias.stream()
                .filter(b -> porBateriaId.containsKey(b.getId()))
                .map(b -> {
                    List<RegistroTempo> tb = porBateriaId.get(b.getId());
                    double melhorBateria = tb.stream()
                            .mapToDouble(RegistroTempo::getTempoEfetivo)
                            .min().orElse(0.0);
                    return new BateriaTempoItem(b.getId(), b.getNumero(), b.getTipo(), melhorBateria, tb.size());
                })
                .toList();

        // Melhor tempo da bateria mais recente (critério de classificação — reseta por bateria)
        List<RegistroTempo> registrosBateriaAtual = todasBaterias.stream()
                .filter(b -> porBateriaId.containsKey(b.getId()))
                .max(Comparator.comparingInt(Bateria::getNumero))
                .map(b -> porBateriaId.get(b.getId()))
                .orElse(tempos);

        double melhorBateriaAtual = registrosBateriaAtual.stream()
                .mapToDouble(RegistroTempo::getTempoEfetivo)
                .min().orElse(melhor);

        // Tempo da última descida individual (corrida com maior ordem na bateria mais recente)
        double ultimaDescida = registrosBateriaAtual.stream()
                .max(Comparator.comparingInt(rt -> rt.getCorrida().getOrdem()))
                .map(RegistroTempo::getTempoEfetivo)
                .orElse(melhorBateriaAtual);

        return new RankingItemResponse(0, equipeId, equipeNome, equipeCurso,
                tempos.size(), melhor, melhorBateriaAtual, ultimaDescida, media, porBateria);
    }
}
