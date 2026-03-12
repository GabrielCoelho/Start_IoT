package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Usuario;
import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import br.edu.fatec.startiot.dto.request.UsuarioRequest;
import br.edu.fatec.startiot.dto.response.UsuarioResponse;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UsuarioService usuarioService;

    private Usuario buildUsuario(Long id, String email, PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("João Silva");
        u.setEmail(email);
        u.setSenhaHash("hash");
        u.setPerfil(perfil);
        u.setAtivo(true);
        return u;
    }

    @Test
    void deveCriarUsuario() {
        var request = new UsuarioRequest("João Silva", "joao@fatec.br", "senha1234", PerfilUsuario.CRONOMETRISTA);
        Usuario salvo = buildUsuario(1L, "joao@fatec.br", PerfilUsuario.CRONOMETRISTA);

        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash_bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(salvo);

        UsuarioResponse response = usuarioService.criar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("joao@fatec.br");
        assertThat(response.perfil()).isEqualTo(PerfilUsuario.CRONOMETRISTA);
        verify(passwordEncoder).encode("senha1234");
    }

    @Test
    void deveLancarConflictSeEmailJaExiste() {
        var request = new UsuarioRequest("João", "joao@fatec.br", "senha1234", PerfilUsuario.CRONOMETRISTA);
        when(usuarioRepository.existsByEmail("joao@fatec.br")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("joao@fatec.br");
    }

    @Test
    void deveBuscarPorId() {
        Usuario usuario = buildUsuario(1L, "joao@fatec.br", PerfilUsuario.ORGANIZADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveListarTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                buildUsuario(1L, "a@fatec.br", PerfilUsuario.CRONOMETRISTA),
                buildUsuario(2L, "b@fatec.br", PerfilUsuario.ORGANIZADOR)
        ));

        assertThat(usuarioService.listar()).hasSize(2);
    }

    @Test
    void deveAtivarUsuario() {
        Usuario usuario = buildUsuario(1L, "joao@fatec.br", PerfilUsuario.CRONOMETRISTA);
        usuario.setAtivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);

        UsuarioResponse response = usuarioService.ativar(1L);

        assertThat(response.ativo()).isTrue();
    }

    @Test
    void deveDesativarUsuario() {
        Usuario usuario = buildUsuario(1L, "joao@fatec.br", PerfilUsuario.CRONOMETRISTA);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = usuarioService.desativar(1L);

        assertThat(response.ativo()).isFalse();
    }

    @Test
    void deveBuscarEntidadePorEmail() {
        Usuario usuario = buildUsuario(1L, "joao@fatec.br", PerfilUsuario.ORGANIZADOR);
        when(usuarioRepository.findByEmail("joao@fatec.br")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarEntidadePorEmail("joao@fatec.br");

        assertThat(resultado.getEmail()).isEqualTo("joao@fatec.br");
    }

    @Test
    void deveLancarNotFoundAoBuscarEmailInexistente() {
        when(usuarioRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarEntidadePorEmail("x@x.com"))
                .isInstanceOf(NotFoundException.class);
    }
}
