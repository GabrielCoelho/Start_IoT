package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.AlocacaoEquipeCorrida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlocacaoEquipeCorridaRepository extends JpaRepository<AlocacaoEquipeCorrida, Long> {

    /** Retorna todas as alocações de uma corrida, ordenadas por data de criação. */
    List<AlocacaoEquipeCorrida> findByCorridaIdOrderByDataCriacao(Long corridaId);

    /** Verifica se existe ao menos uma alocação para a corrida (usado para decidir se a validação é aplicada). */
    boolean existsByCorridaId(Long corridaId);

    /** Verifica se uma equipe específica está alocada em uma corrida. */
    boolean existsByCorridaIdAndEquipeId(Long corridaId, Long equipeId);

    /** Busca a alocação de uma equipe em uma corrida para possibilitar a remoção. */
    Optional<AlocacaoEquipeCorrida> findByCorridaIdAndEquipeId(Long corridaId, Long equipeId);
}
