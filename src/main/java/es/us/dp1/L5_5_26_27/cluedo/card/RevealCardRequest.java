package es.us.dp1.L5_5_26_27.cluedo.card;

import jakarta.validation.constraints.NotNull;

public record RevealCardRequest(@NotNull Integer cardId) {
}
