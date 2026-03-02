package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Usuario;
import br.edu.fatec.startiot.domain.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByPerfil(PerfilUsuario perfil);

    List<Usuario> findByAtivo(Boolean ativo);

    boolean existsByEmail(String email);
}
