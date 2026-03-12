package br.edu.fatec.startiot.dto.response;

import br.edu.fatec.startiot.domain.enums.PerfilUsuario;

import java.time.LocalDateTime;

public record TokenResponse(
        String token,
        String tipo,
        LocalDateTime expiracao,
        Long usuarioId,
        String nomeUsuario,
        PerfilUsuario perfil
) {}
