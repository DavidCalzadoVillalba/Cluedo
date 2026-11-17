package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

public record BonusCardResponse(Integer id, BonusCardEffect effect, boolean immediate) {

	public static BonusCardResponse from(BonusCard card) {
		return card == null ? null : new BonusCardResponse(card.getId(), card.getEffect(), card.isImmediate());
	}
}
