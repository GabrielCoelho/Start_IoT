package br.edu.fatec.startiot.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Dados necessários para pré-alocar uma equipe em uma corrida.
 *
 * @param equipeId ID da equipe a ser alocada. Deve pertencer à mesma edição da corrida
 *                 e estar com status APROVADA.
 */
public record AlocacaoEquipeCorridaRequest(

    @NotNull(message = "Equipe é obrigatória")
    Long equipeId

) {}
