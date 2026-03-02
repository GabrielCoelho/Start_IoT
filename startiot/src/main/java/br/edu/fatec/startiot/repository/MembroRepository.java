package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Membro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembroRepository extends JpaRepository<Membro, Long> {

    List<Membro> findByEquipeId(Long equipeId);

    Optional<Membro> findByRa(String ra);

    boolean existsByRaAndEquipeId(String ra, Long equipeId);
}
