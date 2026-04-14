package br.edu.fatec.startiot.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Representa a pré-alocação de uma equipe em uma corrida antes do seu início.
 *
 * <p>Ao criar alocações para uma corrida, o sistema passa a exigir que somente
 * as equipes alocadas possam ter tempos registrados naquela corrida, evitando
 * erros de vinculação durante a cronometragem. Corridas sem nenhuma alocação
 * mantêm o comportamento anterior (qualquer equipe aprovada pode ter tempo registrado).
 *
 * <p>A alocação só pode ser feita enquanto a corrida estiver com status AGUARDANDO.
 * Uma vez iniciada, a lista de equipes torna-se imutável.
 */
@Entity
@Table(
    name = "alocacoes_equipe_corrida",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_alocacao_corrida_equipe",
            columnNames = {"corrida_id", "equipe_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"corrida", "equipe"})
public class AlocacaoEquipeCorrida extends BaseEntity {

    /** Corrida à qual a equipe está sendo pré-alocada. */
    @NotNull(message = "Corrida é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrida_id", nullable = false)
    private Corrida corrida;

    /** Equipe pré-alocada para participar da corrida. Deve estar com status APROVADA. */
    @NotNull(message = "Equipe é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;
}
