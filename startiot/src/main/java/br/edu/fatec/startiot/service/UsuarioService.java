package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Usuario;
import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import br.edu.fatec.startiot.dto.request.UsuarioRequest;
import br.edu.fatec.startiot.dto.response.UsuarioResponse;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email '%s' já está em uso".formatted(request.email()));
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(true);

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarPorPerfil(PerfilUsuario perfil) {
        return usuarioRepository.findByPerfil(perfil).stream().map(this::toResponse).toList();
    }

    @Transactional
    public UsuarioResponse ativar(Long id) {
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(true);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse desativar(Long id) {
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(false);
        return toResponse(usuarioRepository.save(usuario));
    }

    // Uso interno (AuthService)
    public Usuario buscarEntidadePorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário com email '%s' não encontrado".formatted(email)));
    }

    public Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Usuário", id));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getPerfil(),
                u.getAtivo(),
                u.getDataCriacao(),
                u.getDataAtualizacao()
        );
    }
}
