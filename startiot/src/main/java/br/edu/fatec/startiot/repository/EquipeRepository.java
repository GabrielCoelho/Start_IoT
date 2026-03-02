package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    List<Equipe> findByEdicaoId(Long edicaoId);

    List<Equipe> findByEdicaoIdAndStatusInscricao(Long edicaoId, StatusEquipe status);

    List<Equipe> findByEdicaoIdAndCategoria(Long edicaoId, String categoria);

    boolean existsByEdicaoIdAndNome(Long edicaoId, String nome);
}
