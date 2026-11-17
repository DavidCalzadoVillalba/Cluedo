package es.us.dp1.L5_5_26_27.cluedo.turn;

import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardResponse;

public record TurnActionResponse(Turn turn, BonusCardResponse drawnBonusCard, boolean landedOnBonus) {
}
