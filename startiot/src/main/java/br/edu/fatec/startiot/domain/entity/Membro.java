package br.edu.fatec.startiot.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "membros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "equipe")
public class Membro extends BaseEntity {

    @NotNull(message = "Equipe é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 20)
    private String ra;

    @Column(length = 50)
    private String funcao;
}
