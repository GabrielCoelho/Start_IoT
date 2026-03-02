package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Usuario;
import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario organizador;
    private Usuario cronometrista;

    @BeforeEach
    void setUp() {
        organizador = new Usuario();
        organizador.setNome("Gabriel Coelho");
        organizador.setEmail("gabriel@fatec.sp.gov.br");
        organizador.setSenhaHash("$2a$10$hashOrganizador");
        organizador.setPerfil(PerfilUsuario.ORGANIZADOR);

        cronometrista = new Usuario();
        cronometrista.setNome("Brenda Lima");
        cronometrista.setEmail("brenda@fatec.sp.gov.br");
        cronometrista.setSenhaHash("$2a$10$hashCronometrista");
        cronometrista.setPerfil(PerfilUsuario.CRONOMETRISTA);
    }

    @Test
    void deveSalvarUsuario() {
        Usuario salvo = usuarioRepository.save(organizador);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Gabriel Coelho");
        assertThat(salvo.getAtivo()).isTrue();
    }

    @Test
    void deveBuscarPorEmail() {
        entityManager.persistAndFlush(organizador);

        Optional<Usuario> resultado = usuarioRepository.findByEmail("gabriel@fatec.sp.gov.br");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Gabriel Coelho");
    }

    @Test
    void deveRetornarVazioParaEmailInexistente() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("inexistente@fatec.sp.gov.br");

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarPorPerfil() {
        entityManager.persistAndFlush(organizador);
        entityManager.persistAndFlush(cronometrista);

        List<Usuario> organizadores = usuarioRepository.findByPerfil(PerfilUsuario.ORGANIZADOR);
        List<Usuario> cronometristas = usuarioRepository.findByPerfil(PerfilUsuario.CRONOMETRISTA);

        assertThat(organizadores).hasSize(1);
        assertThat(cronometristas).hasSize(1);
    }

    @Test
    void deveBuscarPorAtivo() {
        entityManager.persistAndFlush(organizador);

        Usuario inativo = new Usuario();
        inativo.setNome("Inativo");
        inativo.setEmail("inativo@fatec.sp.gov.br");
        inativo.setSenhaHash("hash");
        inativo.setPerfil(PerfilUsuario.CRONOMETRISTA);
        inativo.setAtivo(false);
        entityManager.persistAndFlush(inativo);

        assertThat(usuarioRepository.findByAtivo(true)).hasSize(1);
        assertThat(usuarioRepository.findByAtivo(false)).hasSize(1);
    }

    @Test
    void deveVerificarExistenciaPorEmail() {
        entityManager.persistAndFlush(organizador);

        assertThat(usuarioRepository.existsByEmail("gabriel@fatec.sp.gov.br")).isTrue();
        assertThat(usuarioRepository.existsByEmail("outro@fatec.sp.gov.br")).isFalse();
    }
}
