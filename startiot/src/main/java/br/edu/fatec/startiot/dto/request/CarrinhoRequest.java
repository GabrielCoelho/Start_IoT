package br.edu.fatec.startiot.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CarrinhoRequest(

        @NotNull(message = "Equipe é obrigatória")
        Long equipeId,

        @Size(max = 50, message = "Identificação deve ter no máximo 50 caracteres")
        String identificacao
) {}
