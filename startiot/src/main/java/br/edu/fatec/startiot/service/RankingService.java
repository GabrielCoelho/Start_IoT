package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.RegistroTempo;
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

        List<RegistroTempo> tempos = registroTempoRepository.findTemposValidadosPorEdicao(edicaoId);

        Map<Equipe, List<RegistroTempo>> porEquipe = tempos.stream()
                .collect(Collectors.groupingBy(RegistroTempo::getEquipe));

        AtomicInteger posicao = new AtomicInteger(1);

        List<RankingItemResponse> classificacao = porEquipe.entrySet().stream()
                .map(entry -> calcularItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(RankingItemResponse::melhorTempo))
                .map(item -> new RankingItemResponse(
                        posicao.getAndIncrement(),
                        item.equipeId(),
                        item.equipeNome(),
                        item.equipeCurso(),
                        item.totalDescidas(),
                        item.melhorTempo(),
                        item.mediaTempo()
                ))
                .toList();

        return new RankingResponse(
                edicao.getId(),
                edicao.getAno(),
                edicao.getEvento().getNome(),
                classificacao
        );
    }

    private RankingItemResponse calcularItem(Equipe equipe, List<RegistroTempo> tempos) {
        double melhor = tempos.stream()
                .mapToDouble(RegistroTempo::getTempoMilissegundos)
                .min()
                .orElse(0.0);

        double media = tempos.stream()
                .mapToDouble(RegistroTempo::getTempoMilissegundos)
                .average()
                .orElse(0.0);

        return new RankingItemResponse(
                0, // posição preenchida após ordenação
                equipe.getId(),
                equipe.getNome(),
                equipe.getCurso(),
                tempos.size(),
                melhor,
                media
        );
    }
}
