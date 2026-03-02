package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EdicaoRepository extends JpaRepository<Edicao, Long> {

    List<Edicao> findByEventoId(Long eventoId);

    List<Edicao> findByStatus(StatusEdicao status);

    Optional<Edicao> findByEventoIdAndAno(Long eventoId, Integer ano);

    boolean existsByEventoIdAndAno(Long eventoId, Integer ano);
}
