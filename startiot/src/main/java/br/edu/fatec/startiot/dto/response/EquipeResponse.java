package br.edu.fatec.startiot.dto.response;

import br.edu.fatec.startiot.domain.enums.StatusEquipe;

import java.time.LocalDateTime;

public record EquipeResponse(
        Long id,
        Long edicaoId,
        String nome,
        String curso,
        String categoria,
        StatusEquipe statusInscricao,
        Integer totalMembros,
        Boolean carrinhoAprovado,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
