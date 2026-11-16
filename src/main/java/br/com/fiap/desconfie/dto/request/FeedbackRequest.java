package br.com.fiap.desconfie.dto.request;

import br.com.fiap.desconfie.enums.Feedback;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PUT /analises/{id}: o usuário diz se era golpe mesmo ou alarme falso. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "O feedback é obrigatório (ERA_GOLPE ou FALSO_ALARME)")
    private Feedback feedback;
}
