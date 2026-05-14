package br.edu.fatec.startiot.domain.entity;

import br.edu.fatec.startiot.domain.enums.TipoPenalidade;
import br.edu.fatec.startiot.domain.enums.TipoRegistro;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_tempo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"corrida", "equipe", "usuario"})
public class RegistroTempo extends BaseEntity {

    @NotNull(message = "Corrida é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrida_id", nullable = false)
    private Corrida corrida;

    @NotNull(message = "Equipe é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "timestamp_registro")
    private LocalDateTime timestampRegistro;

    @Column(name = "tempo_milissegundos")
    private Double tempoMilissegundos;

    @NotNull(message = "Tipo de registro é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro", nullable = false, length = 20)
    private TipoRegistro tipoRegistro;

    @Column(nullable = false)
    private Boolean validado = false;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_penalidade", length = 10)
    private TipoPenalidade tipoPenalidade;

    @Column(name = "motivo_penalidade", columnDefinition = "TEXT")
    private String motivoPenalidade;

    @Column(name = "penalide_vistoria", nullable = false)
    private Boolean penalideVistoria = false;

    public double getTempoEfetivo() {
        if (tempoMilissegundos == null) return 0.0;
        double base = tempoMilissegundos;
        if (tipoPenalidade != null) base += tipoPenalidade.getValorMs();
        if (Boolean.TRUE.equals(penalideVistoria)) base += 5_000L;
        return base;
    }
}
