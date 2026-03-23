package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import br.edu.fatec.startiot.dto.request.UsuarioRequest;
import br.edu.fatec.startiot.dto.response.UsuarioResponse;
import br.edu.fatec.startiot.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) PerfilUsuario perfil) {
        if (perfil != null) {
            return ResponseEntity.ok(usuarioService.listarPorPerfil(perfil));
        }
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<UsuarioResponse> ativar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.ativar(id));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<UsuarioResponse> desativar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desativar(id));
    }
}
