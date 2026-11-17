package es.us.dp1.L5_5_26_27.cluedo.accusation;

import java.util.List;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.game.GameSolutionResponse;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;

public record AccusationResponse(
		boolean correct,
		GameStatus gameStatus,
		Player winner,
		List<GameCard> revealedHiddenCards,
		GameSolutionResponse solution) {
}
