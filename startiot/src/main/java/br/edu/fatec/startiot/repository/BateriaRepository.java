package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Bateria;
import br.edu.fatec.startiot.domain.enums.StatusBateria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BateriaRepository extends JpaRepository<Bateria, Long> {

    List<Bateria> findByEdicaoIdOrderByNumero(Long edicaoId);

    List<Bateria> findByEdicaoIdAndStatus(Long edicaoId, StatusBateria status);

    boolean existsByEdicaoIdAndNumero(Long edicaoId, Integer numero);
}
