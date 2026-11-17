package es.us.dp1.L5_5_26_27.cluedo.boxMovement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(
		@NotNull(message = "targetBoxId es obligatorio") Integer targetBoxId,
		@Min(value = 1, message = "diceRoll debe ser al menos 1") int diceRoll) {
}
