package br.edu.fatec.startiot.service;

import br.edu.fatec.startiot.domain.entity.Equipe;
import br.edu.fatec.startiot.domain.entity.Membro;
import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import br.edu.fatec.startiot.dto.request.MembroRequest;
import br.edu.fatec.startiot.dto.response.MembroResponse;
import br.edu.fatec.startiot.exception.ConflictException;
import br.edu.fatec.startiot.exception.NotFoundException;
import br.edu.fatec.startiot.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembroServiceTest {

    @Mock private MembroRepository membroRepository;
    @Mock private EquipeService equipeService;
    @InjectMocks private MembroService membroService;

    private Equipe buildEquipe(Long id) {
        Equipe eq = new Equipe();
        eq.setId(id);
        eq.setNome("Team Fatec");
        eq.setStatusInscricao(StatusEquipe.APROVADA);
        eq.setMembros(new ArrayList<>());
        eq.setRegistros(new ArrayList<>());
        return eq;
    }

    private Membro buildMembro(Long id, String ra) {
        Membro m = new Membro();
        m.setId(id);
        m.setEquipe(buildEquipe(1L));
        m.setNome("Ana Silva");
        m.setRa(ra);
        m.setFuncao("piloto");
        return m;
    }

    @Test
    void deveAdicionarMembro() {
        Equipe equipe = buildEquipe(1L);
        Membro salvo = buildMembro(1L, "ADS001");

        when(equipeService.buscarEntidade(1L)).thenReturn(equipe);
        when(membroRepository.existsByRaAndEquipeId("ADS001", 1L)).thenReturn(false);
        when(membroRepository.save(any())).thenReturn(salvo);

        MembroResponse response = membroService.adicionar(new MembroRequest(1L, "Ana Silva", "ADS001", "piloto"));

        assertThat(response.nome()).isEqualTo("Ana Silva");
        assertThat(response.ra()).isEqualTo("ADS001");
    }

    @Test
    void deveLancarConflictSeRaDuplicadoNaEquipe() {
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(membroRepository.existsByRaAndEquipeId("ADS001", 1L)).thenReturn(true);

        assertThatThrownBy(() -> membroService.adicionar(new MembroRequest(1L, "João", "ADS001", "motor")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ADS001");
    }

    @Test
    void deveAdicionarMembroSemRa() {
        Equipe equipe = buildEquipe(1L);
        Membro salvo = buildMembro(1L, null);

        when(equipeService.buscarEntidade(1L)).thenReturn(equipe);
        when(membroRepository.save(any())).thenReturn(salvo);

        MembroResponse response = membroService.adicionar(new MembroRequest(1L, "Ana Silva", null, "piloto"));

        assertThat(response.ra()).isNull();
        verify(membroRepository, never()).existsByRaAndEquipeId(any(), any());
    }

    @Test
    void deveBuscarPorId() {
        when(membroRepository.findById(1L)).thenReturn(Optional.of(buildMembro(1L, "ADS001")));

        MembroResponse response = membroService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarNotFoundAoBuscarIdInexistente() {
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membroService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveListarPorEquipe() {
        when(equipeService.buscarEntidade(1L)).thenReturn(buildEquipe(1L));
        when(membroRepository.findByEquipeId(1L)).thenReturn(List.of(
                buildMembro(1L, "001"), buildMembro(2L, "002")
        ));

        assertThat(membroService.listarPorEquipe(1L)).hasSize(2);
    }

    @Test
    void deveRemoverMembro() {
        Membro membro = buildMembro(1L, "ADS001");
        when(membroRepository.findById(1L)).thenReturn(Optional.of(membro));

        membroService.remover(1L);

        verify(membroRepository).delete(membro);
    }
}
