package br.edu.fatec.startiot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MembroRequest(

        @NotNull(message = "Equipe é obrigatória")
        Long equipeId,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 20, message = "RA deve ter no máximo 20 caracteres")
        String ra,

        @Size(max = 50, message = "Função deve ter no máximo 50 caracteres")
        String funcao
) {}
