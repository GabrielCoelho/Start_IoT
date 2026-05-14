package br.edu.fatec.startiot.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bateria_eliminacao",
       uniqueConstraints = @UniqueConstraint(columnNames = {"bateria_id", "equipe_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BateriaEliminacao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bateria_id", nullable = false)
    private Bateria bateria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    /** Posição em que a equipe foi eliminada nesta bateria */
    @Column(nullable = false)
    private Integer posicao;
}
