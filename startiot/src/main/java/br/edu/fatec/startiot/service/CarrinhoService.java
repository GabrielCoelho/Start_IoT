package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Carrinho;
import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import br.edu.fatec.startiot.dto.request.CarrinhoRequest;
import br.edu.fatec.startiot.dto.request.VistoriaRequest;
import br.edu.fatec.startiot.dto.response.CarrinhoResponse;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.CarrinhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final EquipeService equipeService;

    @Transactional
    public CarrinhoResponse cadastrar(CarrinhoRequest request) {
        Equipe equipe = equipeService.buscarEntidade(request.equipeId());

        if (carrinhoRepository.existsByEquipeId(request.equipeId())) {
            throw new ConflictException(
                    "Equipe '%s' já possui um carrinho cadastrado".formatted(equipe.getNome())
            );
        }

        Carrinho carrinho = new Carrinho();
        carrinho.setEquipe(equipe);
        carrinho.setIdentificacao(request.identificacao());
        carrinho.setAprovadoVistoria(false);

        return toResponse(carrinhoRepository.save(carrinho));
    }

    @Transactional(readOnly = true)
    public CarrinhoResponse buscarPorEquipe(Long equipeId) {
        equipeService.buscarEntidade(equipeId);
        Carrinho carrinho = carrinhoRepository.findByEquipeId(equipeId)
                .orElseThrow(() -> new NotFoundException(
                        "Carrinho não encontrado para a equipe de id %d".formatted(equipeId)
                ));
        return toResponse(carrinho);
    }

    @Transactional
    public CarrinhoResponse registrarVistoria(Long equipeId, VistoriaRequest request) {
        Equipe equipe = equipeService.buscarEntidade(equipeId);
        Carrinho carrinho = carrinhoRepository.findByEquipeId(equipeId)
                .orElseThrow(() -> new NotFoundException(
                        "Carrinho não encontrado para a equipe de id %d".formatted(equipeId)
                ));

        boolean aprovado = Boolean.TRUE.equals(request.aprovado());
        carrinho.setAprovadoVistoria(aprovado);
        carrinho.setPenalideVistoria(Boolean.TRUE.equals(request.penalidade()) && aprovado);
        carrinho.setObservacoesVistoria(request.observacoes());
        carrinho.setDataVistoria(LocalDateTime.now());

        if (!aprovado) {
            equipeService.atualizarStatusPorVistoria(equipe.getId(), StatusEquipe.REPROVADA);
        } else if (equipe.getStatusInscricao() == StatusEquipe.REPROVADA) {
            equipeService.atualizarStatusPorVistoria(equipe.getId(), StatusEquipe.APROVADA);
        }

        return toResponse(carrinhoRepository.save(carrinho));
    }

    @Transactional(readOnly = true)
    public List<CarrinhoResponse> listarPorEdicao(Long edicaoId) {
        return carrinhoRepository.findByEquipeEdicaoId(edicaoId)
                .stream().map(this::toResponse).toList();
    }

    private CarrinhoResponse toResponse(Carrinho c) {
        return new CarrinhoResponse(
                c.getId(),
                c.getEquipe().getId(),
                c.getEquipe().getNome(),
                c.getIdentificacao(),
                c.getAprovadoVistoria(),
                c.getPenalideVistoria(),
                c.getObservacoesVistoria(),
                c.getDataVistoria(),
                c.getDataCriacao(),
                c.getDataAtualizacao()
        );
    }
}
