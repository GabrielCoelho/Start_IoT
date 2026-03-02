package br.edu.fatec.startiot.repository;

import br.edu.fatec.startiot.domain.entity.RegistroTempo;
import br.edu.fatec.startiot.domain.enums.TipoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroTempoRepository extends JpaRepository<RegistroTempo, Long> {

    List<RegistroTempo> findByCorridaId(Long corridaId);

    List<RegistroTempo> findByEquipeIdAndCorridaId(Long equipeId, Long corridaId);

    List<RegistroTempo> findByCorridaIdAndTipoRegistro(Long corridaId, TipoRegistro tipo);

    List<RegistroTempo> findByValidado(Boolean validado);

    List<RegistroTempo> findByEquipeIdOrderByTempoMilissegundosAsc(Long equipeId);
}
