package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;

public record NeutralCardResponse(Integer id, GameCard revealedCard, boolean availableToConsult) {
	public static NeutralCardResponse hidden(Integer id) {
		return new NeutralCardResponse(id, null, true);
	}
}
