package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.RegistroTempo;
import br.edu.fatec.startiot.domain.enums.TipoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroTempoRepository extends JpaRepository<RegistroTempo, Long> {

    List<RegistroTempo> findByCorridaId(Long corridaId);

    List<RegistroTempo> findByEquipeIdAndCorridaId(Long equipeId, Long corridaId);

    List<RegistroTempo> findByCorridaIdAndTipoRegistro(Long corridaId, TipoRegistro tipo);

    List<RegistroTempo> findByValidado(Boolean validado);

    List<RegistroTempo> findByEquipeIdOrderByTempoMilissegundosAsc(Long equipeId);

    @Query("SELECT rt FROM RegistroTempo rt " +
           "JOIN rt.corrida c JOIN c.bateria b " +
           "WHERE b.edicao.id = :edicaoId " +
           "AND rt.validado = true " +
           "AND rt.tipoRegistro = 'CHEGADA'")
    List<RegistroTempo> findTemposValidadosPorEdicao(@Param("edicaoId") Long edicaoId);
}
