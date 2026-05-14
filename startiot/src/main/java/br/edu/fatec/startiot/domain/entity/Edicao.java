package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusEdicao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "edicoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"evento", "equipes", "baterias"})
public class Edicao extends BaseEntity {

    @NotNull(message = "Evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @NotNull(message = "Ano é obrigatório")
    @Column(nullable = false)
    private Integer ano;

    @Column
    private Integer numero;

    @Column(name = "data_evento")
    private LocalDate dataEvento;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEdicao status;

    @OneToMany(mappedBy = "edicao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Equipe> equipes = new ArrayList<>();

    @OneToMany(mappedBy = "edicao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bateria> baterias = new ArrayList<>();
}
