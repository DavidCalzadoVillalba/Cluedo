package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import jakarta.validation.constraints.NotNull;

public record RefuteRequest(
		@NotNull Integer cardId) {
}

