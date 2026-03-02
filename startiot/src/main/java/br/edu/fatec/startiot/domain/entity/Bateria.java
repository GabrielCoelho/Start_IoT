package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusBateria;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "baterias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"edicao", "corridas"})
public class Bateria extends BaseEntity {

    @NotNull(message = "Edição é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edicao_id", nullable = false)
    private Edicao edicao;

    @Column(nullable = false)
    private Integer numero;

    @Column(length = 50)
    private String tipo;

    @Column(name = "horario_previsto")
    private LocalDateTime horarioPrevisto;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusBateria status;

    @OneToMany(mappedBy = "bateria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Corrida> corridas = new ArrayList<>();
}
