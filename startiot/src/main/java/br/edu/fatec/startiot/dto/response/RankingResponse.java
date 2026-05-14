package br.edu.fatec.startiot.dto.response;

import java.util.List;

public record RankingResponse(
        Long edicaoId,
        Integer anoEdicao,
        String nomeEvento,
        List<BateriaInfo> baterias,
        List<RankingItemResponse> classificacao
) {}
