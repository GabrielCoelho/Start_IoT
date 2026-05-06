package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Corrida;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.RegistroTempo;
import br.edu.fatec.startiot.domain.entity.Usuario;
import br.edu.fatec.startiot.domain.enums.StatusCorrida;
import br.edu.fatec.startiot.dto.request.RegistroTempoRequest;
import br.edu.fatec.startiot.dto.response.RegistroTempoResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.AlocacaoEquipeCorridaRepository;
import br.edu.fatec.startiot.repository.RegistroTempoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroTempoService {

    private final RegistroTempoRepository registroTempoRepository;
    private final AlocacaoEquipeCorridaRepository alocacaoRepository;
    private final CorridaService corridaService;
    private final EquipeService equipeService;
    private final UsuarioService usuarioService;

    @Transactional
    public RegistroTempoResponse registrar(RegistroTempoRequest request, Long usuarioId) {
        Corrida corrida = corridaService.buscarEntidade(request.corridaId());

        if (corrida.getStatus() != StatusCorrida.EM_ANDAMENTO) {
            throw new BusinessException(
                    "Não é possível registrar tempo: corrida está com status '%s'".formatted(corrida.getStatus())
            );
        }

        Equipe equipe = equipeService.buscarEntidade(request.equipeId());

        // Se a corrida possui pré-alocações, apenas as equipes alocadas podem ter tempos registrados.
        // Corridas sem nenhuma alocação mantêm o comportamento original (aceita qualquer equipe aprovada).
        if (alocacaoRepository.existsByCorridaId(corrida.getId())
                && !alocacaoRepository.existsByCorridaIdAndEquipeId(corrida.getId(), equipe.getId())) {
            throw new BusinessException(
                    "Equipe '%s' não está pré-alocada nesta corrida. " +
                    "Verifique as alocações em GET /api/corridas/%d/equipes."
                    .formatted(equipe.getNome(), corrida.getId())
            );
        }
        Usuario usuario = usuarioService.buscarEntidade(usuarioId);

        RegistroTempo registro = new RegistroTempo();
        registro.setCorrida(corrida);
        registro.setEquipe(equipe);
        registro.setUsuario(usuario);
        registro.setTempoMilissegundos(request.tempoMilissegundos());
        registro.setTipoRegistro(request.tipoRegistro());
        registro.setObservacoes(request.observacoes());
        registro.setTimestampRegistro(LocalDateTime.now());
        registro.setValidado(true);

        return toResponse(registroTempoRepository.save(registro));
    }

    @Transactional(readOnly = true)
    public RegistroTempoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<RegistroTempoResponse> listarPorCorrida(Long corridaId) {
        corridaService.buscarEntidade(corridaId);
        return registroTempoRepository.findByCorridaId(corridaId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RegistroTempoResponse validar(Long id) {
        RegistroTempo registro = buscarEntidade(id);
        if (registro.getValidado()) {
            throw new BusinessException("Registro de tempo já está validado");
        }
        registro.setValidado(true);
        return toResponse(registroTempoRepository.save(registro));
    }

    @Transactional
    public RegistroTempoResponse invalidar(Long id) {
        RegistroTempo registro = buscarEntidade(id);
        if (!registro.getValidado()) {
            throw new BusinessException("Registro de tempo já está invalidado");
        }
        registro.setValidado(false);
        return toResponse(registroTempoRepository.save(registro));
    }

    public RegistroTempo buscarEntidade(Long id) {
        return registroTempoRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("RegistroTempo", id));
    }

    private RegistroTempoResponse toResponse(RegistroTempo r) {
        return new RegistroTempoResponse(
                r.getId(),
                r.getCorrida().getId(),
                r.getEquipe().getId(),
                r.getEquipe().getNome(),
                r.getUsuario().getId(),
                r.getUsuario().getNome(),
                r.getTimestampRegistro(),
                r.getTempoMilissegundos(),
                r.getTipoRegistro(),
                r.getValidado(),
                r.getObservacoes(),
                r.getDataCriacao(),
                r.getDataAtualizacao()
        );
    }
}
