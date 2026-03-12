package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.Membro;
import br.edu.fatec.startiot.dto.request.MembroRequest;
import br.edu.fatec.startiot.dto.response.MembroResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembroService {

    private final MembroRepository membroRepository;
    private final EquipeService equipeService;

    @Transactional
    public MembroResponse adicionar(MembroRequest request) {
        Equipe equipe = equipeService.buscarEntidade(request.equipeId());

        if (request.ra() != null && !request.ra().isBlank()
                && membroRepository.existsByRaAndEquipeId(request.ra(), request.equipeId())) {
            throw new ConflictException(
                    "RA '%s' já está cadastrado nesta equipe".formatted(request.ra())
            );
        }

        Membro membro = new Membro();
        membro.setEquipe(equipe);
        membro.setNome(request.nome());
        membro.setRa(request.ra());
        membro.setFuncao(request.funcao());

        return toResponse(membroRepository.save(membro));
    }

    @Transactional(readOnly = true)
    public MembroResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<MembroResponse> listarPorEquipe(Long equipeId) {
        equipeService.buscarEntidade(equipeId);
        return membroRepository.findByEquipeId(equipeId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void remover(Long id) {
        Membro membro = buscarEntidade(id);
        membroRepository.delete(membro);
    }

    @Transactional
    public MembroResponse atualizar(Long id, MembroRequest request) {
        Membro membro = buscarEntidade(id);

        boolean raAlterado = request.ra() != null && !request.ra().equals(membro.getRa());
        if (raAlterado && membroRepository.existsByRaAndEquipeId(request.ra(), membro.getEquipe().getId())) {
            throw new ConflictException("RA '%s' já está cadastrado nesta equipe".formatted(request.ra()));
        }

        membro.setNome(request.nome());
        membro.setRa(request.ra());
        membro.setFuncao(request.funcao());

        return toResponse(membroRepository.save(membro));
    }

    private Membro buscarEntidade(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Membro", id));
    }

    private MembroResponse toResponse(Membro m) {
        return new MembroResponse(
                m.getId(),
                m.getEquipe().getId(),
                m.getEquipe().getNome(),
                m.getNome(),
                m.getRa(),
                m.getFuncao(),
                m.getDataCriacao(),
                m.getDataAtualizacao()
        );
    }

    // Regra de negócio futura: validar quantidade máxima de membros por equipe
    private void validarLimiteMembros(Equipe equipe) {
        int total = membroRepository.findByEquipeId(equipe.getId()).size();
        if (total >= 5) {
            throw new BusinessException("Equipe já possui o número máximo de 5 membros");
        }
    }
}
