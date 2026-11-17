package es.us.dp1.L5_5_26_27.cluedo.game;

import java.util.List;

public record ActiveGameResponse(
		Integer id,
		String name,
		Integer participantCount,
		Integer maxPlayers,
		List<String> players,
		GameStatus status) {

	static ActiveGameResponse from(Game game) {
		List<String> players = game.getPlayers().stream()
				.map(player -> player.getUser().getUsername())
				.toList();
		return new ActiveGameResponse(game.getId(), game.getName(), players.size(), game.getMaxPlayers(), players,
				game.getStatus());
	}
}
