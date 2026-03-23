package br.edu.fatec.startiot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipeRequest(

        @NotNull(message = "Edição é obrigatória")
        Long edicaoId,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 100, message = "Curso deve ter no máximo 100 caracteres")
        String curso,

        @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
        String categoria
) {}
