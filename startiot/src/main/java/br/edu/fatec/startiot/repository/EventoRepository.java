package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    Optional<Evento> findByNome(String nome);

    boolean existsByNome(String nome);
}
