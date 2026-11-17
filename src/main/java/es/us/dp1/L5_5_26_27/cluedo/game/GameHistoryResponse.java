package es.us.dp1.L5_5_26_27.cluedo.game;

import java.time.Instant;
import java.util.List;

import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.user.User;

public record GameHistoryResponse(
		Integer id,
		String name,
		Instant playedAt,
		List<String> participants,
		boolean won) {

	public static GameHistoryResponse from(Game game, User user) {
		boolean won = game.getWinner() != null && game.getWinner().getUser().getId().equals(user.getId());
		return new GameHistoryResponse(
				game.getId(),
				game.getName(),
				game.getEndedAt(),
				game.getPlayers().stream().map(Player::getUser).map(User::getUsername).toList(),
				won);
	}
}
