package br.edu.fatec.startiot.domain.entity;

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
}
