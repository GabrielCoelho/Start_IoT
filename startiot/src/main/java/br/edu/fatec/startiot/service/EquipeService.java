package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Edicao;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import br.edu.fatec.startiot.dto.request.EquipeRequest;
import br.edu.fatec.startiot.dto.response.EquipeResponse;
import br.edu.fatec.startiot.exception.BusinessException;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.domain.entity.Carrinho;
import br.edu.fatec.startiot.repository.CarrinhoRepository;
import br.edu.fatec.startiot.repository.EquipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final EdicaoService edicaoService;

    @Transactional
    public EquipeResponse inscrever(EquipeRequest request) {
        Edicao edicao = edicaoService.buscarEntidade(request.edicaoId());

        if (edicao.getStatus() != StatusEdicao.PLANEJADA) {
            throw new BusinessException(
                    "Inscrições encerradas: edição está com status '%s'".formatted(edicao.getStatus())
            );
        }

        if (equipeRepository.existsByEdicaoIdAndNome(request.edicaoId(), request.nome())) {
            throw new ConflictException(
                    "Já existe uma equipe com nome '%s' nesta edição".formatted(request.nome())
            );
        }

        Equipe equipe = new Equipe();
        equipe.setEdicao(edicao);
        equipe.setNome(request.nome());
        equipe.setCurso(request.curso());
        equipe.setCategoria(request.categoria());
        equipe.setStatusInscricao(StatusEquipe.PENDENTE);

        return toResponse(equipeRepository.save(equipe));
    }

    @Transactional(readOnly = true)
    public EquipeResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> listarPorEdicao(Long edicaoId) {
        edicaoService.buscarEntidade(edicaoId);
        return equipeRepository.findByEdicaoId(edicaoId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> listarAprovadas(Long edicaoId) {
        return equipeRepository
                .findByEdicaoIdAndStatusInscricao(edicaoId, StatusEquipe.APROVADA)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public EquipeResponse aprovar(Long id) {
        Equipe equipe = buscarEntidade(id);
        if (equipe.getStatusInscricao() != StatusEquipe.PENDENTE) {
            throw new BusinessException(
                    "Não é possível aprovar uma equipe com status '%s'".formatted(equipe.getStatusInscricao())
            );
        }
        equipe.setStatusInscricao(StatusEquipe.APROVADA);
        equipeRepository.save(equipe);

        if (!carrinhoRepository.existsByEquipeId(equipe.getId())) {
            Carrinho carrinho = new Carrinho();
            carrinho.setEquipe(equipe);
            carrinho.setIdentificacao(equipe.getNome());
            carrinho.setAprovadoVistoria(false);
            carrinho.setPenalideVistoria(false);
            carrinhoRepository.save(carrinho);
        }

        return toResponse(equipe);
    }

    @Transactional
    public EquipeResponse reprovar(Long id) {
        return alterarStatus(id, StatusEquipe.REPROVADA, StatusEquipe.PENDENTE, "reprovar");
    }

    @Transactional
    public EquipeResponse cancelar(Long id) {
        Equipe equipe = buscarEntidade(id);
        if (equipe.getStatusInscricao() == StatusEquipe.CANCELADA) {
            throw new BusinessException("Equipe já está cancelada");
        }
        equipe.setStatusInscricao(StatusEquipe.CANCELADA);
        return toResponse(equipeRepository.save(equipe));
    }

    @Transactional
    public void atualizarStatusPorVistoria(Long equipeId, StatusEquipe novoStatus) {
        Equipe equipe = buscarEntidade(equipeId);
        equipe.setStatusInscricao(novoStatus);
        equipeRepository.save(equipe);
    }

    public Equipe buscarEntidade(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Equipe", id));
    }

    private EquipeResponse alterarStatus(Long id, StatusEquipe novo, StatusEquipe esperado, String acao) {
        Equipe equipe = buscarEntidade(id);
        if (equipe.getStatusInscricao() != esperado) {
            throw new BusinessException(
                    "Não é possível %s uma equipe com status '%s'".formatted(acao, equipe.getStatusInscricao())
            );
        }
        equipe.setStatusInscricao(novo);
        return toResponse(equipeRepository.save(equipe));
    }

    private EquipeResponse toResponse(Equipe e) {
        Boolean carrinhoAprovado = e.getCarrinho() != null ? e.getCarrinho().getAprovadoVistoria() : null;
        return new EquipeResponse(
                e.getId(),
                e.getEdicao().getId(),
                e.getNome(),
                e.getCurso(),
                e.getCategoria(),
                e.getStatusInscricao(),
                e.getMembros().size(),
                carrinhoAprovado,
                e.getDataCriacao(),
                e.getDataAtualizacao()
        );
    }
}
