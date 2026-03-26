package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import br.edu.fatec.startiot.dto.request.UsuarioRequest;
import br.edu.fatec.startiot.dto.response.UsuarioResponse;
import br.edu.fatec.startiot.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema (organizadores, árbitros e juízes)")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
        summary = "Criar usuário",
        description = "Cadastra um novo usuário no sistema. Utilizado pelo administrador para registrar organizadores, " +
                      "árbitros e juízes que irão operar o sistema durante as edições do evento."
    )
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna os dados de um usuário específico. Usado para verificar perfil e status de um operador antes de atribuí-lo a uma função no evento."
    )
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @Operation(
        summary = "Listar usuários",
        description = "Retorna todos os usuários cadastrados. Filtre pelo parâmetro 'perfil' para obter apenas usuários de um papel específico " +
                      "(ex: listar todos os árbitros disponíveis para escalar em uma corrida)."
    )
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @Parameter(description = "Filtra pelo perfil do usuário (ORGANIZADOR, ARBITRO, JUIZ)") @RequestParam(required = false) PerfilUsuario perfil) {
        if (perfil != null) {
            return ResponseEntity.ok(usuarioService.listarPorPerfil(perfil));
        }
        return ResponseEntity.ok(usuarioService.listar());
    }

    @Operation(
        summary = "Ativar usuário",
        description = "Reativa um usuário previamente desativado, permitindo que ele faça login e opere o sistema novamente."
    )
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<UsuarioResponse> ativar(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.ativar(id));
    }

    @Operation(
        summary = "Desativar usuário",
        description = "Desativa um usuário sem removê-lo do banco de dados. O usuário desativado não consegue realizar login. " +
                      "Use quando um operador não faz mais parte da organização do evento."
    )
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<UsuarioResponse> desativar(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desativar(id));
    }
}
