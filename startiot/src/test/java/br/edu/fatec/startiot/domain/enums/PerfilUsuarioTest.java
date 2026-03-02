package br.edu.fatec.startiot.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerfilUsuarioTest {

    @Test
    void deveConterExatamenteTresValores() {
        assertThat(PerfilUsuario.values()).hasSize(3);
    }

    @Test
    void deveConterTodosOsValoresEsperados() {
        assertThat(PerfilUsuario.values()).containsExactlyInAnyOrder(
                PerfilUsuario.ORGANIZADOR,
                PerfilUsuario.CRONOMETRISTA,
                PerfilUsuario.ADMIN
        );
    }

    @Test
    void deveConverterPorNomeCorretamente() {
        assertThat(PerfilUsuario.valueOf("ORGANIZADOR")).isEqualTo(PerfilUsuario.ORGANIZADOR);
        assertThat(PerfilUsuario.valueOf("CRONOMETRISTA")).isEqualTo(PerfilUsuario.CRONOMETRISTA);
        assertThat(PerfilUsuario.valueOf("ADMIN")).isEqualTo(PerfilUsuario.ADMIN);
    }
}
