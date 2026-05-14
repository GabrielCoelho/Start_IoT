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
           "JOIN FETCH rt.corrida c " +
           "JOIN FETCH c.bateria b " +
           "JOIN FETCH rt.equipe e " +
           "WHERE b.edicao.id = :edicaoId " +
           "AND rt.validado = true " +
           "AND rt.tipoRegistro = 'CHEGADA' " +
           "AND c.status = 'FINALIZADA'")
    List<RegistroTempo> findTemposValidadosPorEdicao(@Param("edicaoId") Long edicaoId);

    @Query("SELECT rt FROM RegistroTempo rt " +
           "JOIN FETCH rt.corrida c " +
           "JOIN FETCH c.bateria b " +
           "JOIN FETCH rt.equipe e " +
           "JOIN FETCH rt.usuario u " +
           "WHERE b.edicao.id = :edicaoId " +
           "AND rt.tipoRegistro = 'CHEGADA' " +
           "ORDER BY b.numero, c.ordem, rt.tempoMilissegundos")
    List<RegistroTempo> findChegadasPorEdicao(@Param("edicaoId") Long edicaoId);
}
