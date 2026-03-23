package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.domain.entity.Usuario;
import br.edu.fatec.startiot.dto.request.AuthRequest;
import br.edu.fatec.startiot.dto.response.TokenResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    // TODO: substituir por JWT real na Fase de Segurança
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequest request) {
        Usuario usuario = usuarioService.buscarEntidadePorEmail(request.email());

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BusinessException("Credenciais inválidas");
        }

        if (!usuario.getAtivo()) {
            throw new BusinessException("Usuário inativo");
        }

        TokenResponse response = new TokenResponse(
                UUID.randomUUID().toString(),
                "Bearer",
                LocalDateTime.now().plusHours(8),
                usuario.getId(),
                usuario.getNome(),
                usuario.getPerfil()
        );

        return ResponseEntity.ok(response);
    }
}
