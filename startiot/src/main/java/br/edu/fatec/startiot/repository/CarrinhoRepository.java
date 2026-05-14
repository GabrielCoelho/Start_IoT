package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findByEquipeId(Long equipeId);

    List<Carrinho> findByAprovadoVistoria(Boolean aprovado);

    List<Carrinho> findByEquipeEdicaoId(Long edicaoId);

    boolean existsByEquipeId(Long equipeId);
}
