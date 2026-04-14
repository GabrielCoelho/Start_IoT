package br.edu.fatec.startiot.dto.response;

import java.time.LocalDateTime;

/**
 * Dados retornados ao consultar ou criar uma pré-alocação de equipe em corrida.
 *
 * @param id            ID da alocação
 * @param corridaId     ID da corrida
 * @param corridaOrdem  Número de ordem da corrida dentro da bateria
 * @param equipeId      ID da equipe alocada
 * @param equipeNome    Nome da equipe (desnormalizado para facilitar exibição)
 * @param equipeCurso   Curso da equipe (desnormalizado para facilitar exibição)
 * @param dataCriacao   Data/hora em que a alocação foi registrada
 * @param dataAtualizacao Data/hora da última alteração
 */
public record AlocacaoEquipeCorridaResponse(
    Long id,
    Long corridaId,
    Integer corridaOrdem,
    Long equipeId,
    String equipeNome,
    String equipeCurso,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao
) {}
