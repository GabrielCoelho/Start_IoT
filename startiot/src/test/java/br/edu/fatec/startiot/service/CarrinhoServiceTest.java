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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceTest {

    @Mock private CarrinhoRepository carrinhoRepository;
    @Mock private EquipeService equipeService;
    @InjectMocks private CarrinhoService carrinhoService;

    private Equipe buildEquipe(Long id) {
        Equipe eq = new Equipe();
        eq.setId(id);
        eq.setNome("Team Fatec");
        eq.setStatusInscricao(StatusEquipe.APROVADA);
        eq.setMembros(new ArrayList<>());
        eq.setRegistros(new ArrayList<>());
        return eq;
    }

    private Carrinho buildCarrinho(Long id, Long equipeId) {
        Carrinho c = new Carrinho();
        c.setId(id);
        c.setEquipe(buildEquipe(equipeId));
        c.setIdentificacao("CRR-001");
        c.setAprovadoVistoria(false);
        return c;
    }

    @Test
    void deveCadastrarCarrinho() {
        Equipe equipe = buildEquipe(1L);
        Carrinho salvo = buildCarrinho(1L, 1L);

        when(equipeService.buscarEntidade(1L)).thenReturn(equipe);
        when(carrinhoRepository.existsByEquipeId(1L)).thenReturn(false);
        when(carrinhoRepository.save(any())).thenReturn(salvo);

        CarrinhoResponse response = carrinhoService.cadastrar(new CarrinhoRequest(1L, "CRR-001"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.identificacao()).isEqualTo("CRR-001");
        assertThat(response.aprovadoVistoria()).isFalse();
    }

    @Test
    void deveLancarConflictSeCarrinhoJaExisteParaEquipe() {
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(carrinhoRepository.existsByEquipeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> carrinhoService.cadastrar(new CarrinhoRequest(1L, "CRR-002")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Team Fatec");
    }

    @Test
    void deveBuscarPorEquipe() {
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(carrinhoRepository.findByEquipeId(1L)).thenReturn(Optional.of(buildCarrinho(1L, 1L)));

        CarrinhoResponse response = carrinhoService.buscarPorEquipe(1L);

        assertThat(response.equipeId()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundSeBuscarCarrinhoDeEquipeSemCarrinho() {
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(carrinhoRepository.findByEquipeId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carrinhoService.buscarPorEquipe(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveRegistrarVistoriaAprovada() {
        Carrinho carrinho = buildCarrinho(1L, 1L);
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(carrinhoRepository.findByEquipeId(1L)).thenReturn(Optional.of(carrinho));
        when(carrinhoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CarrinhoResponse response = carrinhoService.registrarVistoria(1L, new VistoriaRequest(true, null, "Tudo ok"));

        assertThat(response.aprovadoVistoria()).isTrue();
        assertThat(response.observacoesVistoria()).isEqualTo("Tudo ok");
        assertThat(response.dataVistoria()).isNotNull();
    }

    @Test
    void deveRegistrarVistoriaReprovada() {
        Carrinho carrinho = buildCarrinho(1L, 1L);
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(carrinhoRepository.findByEquipeId(1L)).thenReturn(Optional.of(carrinho));
        when(carrinhoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CarrinhoResponse response = carrinhoService.registrarVistoria(1L, new VistoriaRequest(false, null, "Freio irregular"));

        assertThat(response.aprovadoVistoria()).isFalse();
        assertThat(response.observacoesVistoria()).isEqualTo("Freio irregular");
    }
}
