package br.edu.fatec.startiot.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "carrinhos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "equipe")
public class Carrinho extends BaseEntity {

    @NotNull(message = "Equipe é obrigatória")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false, unique = true)
    private Equipe equipe;

    @Column(length = 50)
    private String identificacao;

    @Column(name = "aprovado_vistoria", nullable = false)
    private Boolean aprovadoVistoria = false;

    @Column(name = "observacoes_vistoria", columnDefinition = "TEXT")
    private String observacoesVistoria;

    @Column(name = "data_vistoria")
    private LocalDateTime dataVistoria;

    @Column(name = "penalide_vistoria", nullable = false)
    private Boolean penalideVistoria = false;
}
