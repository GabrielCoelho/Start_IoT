package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.BateriaEliminacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BateriaEliminacaoRepository extends JpaRepository<BateriaEliminacao, Long> {

    @Query("SELECT be.equipe.id FROM BateriaEliminacao be WHERE be.bateria.edicao.id = :edicaoId")
    Set<Long> findEquipeIdsEliminadasPorEdicao(@Param("edicaoId") Long edicaoId);
}
