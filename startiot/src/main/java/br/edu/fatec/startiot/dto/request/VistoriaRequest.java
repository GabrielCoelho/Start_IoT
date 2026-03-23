package br.edu.fatec.startiot.dto.request;

import jakarta.validation.constraints.NotNull;

public record VistoriaRequest(

        @NotNull(message = "Resultado da vistoria é obrigatório")
        Boolean aprovado,

        String observacoes
) {}
