package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;

public record BonusCardRevealResponse(
		Integer id,
		Integer targetPlayerId,
		String targetPlayerName,
		Integer requesterPlayerId,
		String requesterPlayerName,
		GameCard revealedCard) {

	public static BonusCardRevealResponse from(BonusCardReveal reveal) {
		Player target = reveal.getTarget();
		Player requester = reveal.getRequester();
		return new BonusCardRevealResponse(
				reveal.getId(),
				target.getId(),
				target.getUser().getUsername(),
				requester.getId(),
				requester.getUser().getUsername(),
				reveal.getRevealedCard());
	}
}
