package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusEquipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"edicao", "membros", "carrinho", "registros"})
public class Equipe extends BaseEntity {

    @NotNull(message = "Edição é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edicao_id", nullable = false)
    private Edicao edicao;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String curso;

    @Column(length = 50)
    private String categoria;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status_inscricao", nullable = false, length = 20)
    private StatusEquipe statusInscricao;

    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Membro> membros = new ArrayList<>();

    @OneToOne(mappedBy = "equipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Carrinho carrinho;

    @OneToMany(mappedBy = "equipe", fetch = FetchType.LAZY)
    private List<RegistroTempo> registros = new ArrayList<>();
}
