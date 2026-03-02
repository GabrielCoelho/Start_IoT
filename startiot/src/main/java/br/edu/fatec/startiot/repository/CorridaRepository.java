package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Corrida;
import br.edu.fatec.startiot.domain.enums.StatusCorrida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorridaRepository extends JpaRepository<Corrida, Long> {

    List<Corrida> findByBateriaIdOrderByOrdem(Long bateriaId);

    List<Corrida> findByBateriaIdAndStatus(Long bateriaId, StatusCorrida status);

    boolean existsByBateriaIdAndOrdem(Long bateriaId, Integer ordem);
}
