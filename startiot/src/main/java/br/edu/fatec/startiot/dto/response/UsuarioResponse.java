package br.edu.fatec.startiot.dto.response;

import br.edu.fatec.startiot.domain.enums.PerfilUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {}
