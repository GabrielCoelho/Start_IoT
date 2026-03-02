package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.StatusCorrida;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "corridas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"bateria", "registros"})
public class Corrida extends BaseEntity {

    @NotNull(message = "Bateria é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bateria_id", nullable = false)
    private Bateria bateria;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "horario_inicio")
    private LocalDateTime horarioInicio;

    @Column(name = "horario_fim")
    private LocalDateTime horarioFim;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCorrida status;

    @OneToMany(mappedBy = "corrida", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RegistroTempo> registros = new ArrayList<>();
}
