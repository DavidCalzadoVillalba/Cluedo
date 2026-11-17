package es.us.dp1.L5_5_26_27.cluedo.game;

import java.util.List;

import es.us.dp1.L5_5_26_27.cluedo.board.Board;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardResponse;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRevealResponse;
import es.us.dp1.L5_5_26_27.cluedo.character.CharacterToken;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.suggestion.SuggestionResponse;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponToken;

public record GameStateResponse(
		Integer id,
		String name,
		String code,
		Integer maxPlayers,
		GameStatus status,
		User host,
		List<Player> players,
		Board board,
		Turn activeTurn,
		List<WeaponToken> weaponTokens,
		List<CharacterToken> characterTokens,
		List<GameCard> myHand,
		List<BonusCardResponse> myBonusCards,
		List<GameCard> faceUpCards,
		boolean solutionSaved,
		Player winner,
		SuggestionResponse activeSuggestion,
		List<BonusCardRevealResponse> pendingBonusReveals,
		List<BonusCardRevealResponse> publicBonusReveals,
		GameSolutionResponse solution) {

	public static GameStateResponse from(
			Game game,
			Turn activeTurn,
			List<WeaponToken> weaponTokens,
			List<GameCard> myHand,
			List<BonusCard> myBonusCards,
			List<GameCard> faceUpCards) {
		return from(game, activeTurn, weaponTokens, game.getCharacterTokens(), myHand, myBonusCards, faceUpCards, null, List.of(), List.of());
	}

	public static GameStateResponse from(
			Game game,
			Turn activeTurn,
			List<WeaponToken> weaponTokens,
			List<GameCard> myHand,
			List<BonusCard> myBonusCards,
			List<GameCard> faceUpCards,
			SuggestionResponse activeSuggestion) {
		return from(game, activeTurn, weaponTokens, game.getCharacterTokens(), myHand, myBonusCards, faceUpCards,
				activeSuggestion, List.of(), List.of());
	}

	public static GameStateResponse from(
			Game game,
			Turn activeTurn,
			List<WeaponToken> weaponTokens,
			List<GameCard> myHand,
			List<BonusCard> myBonusCards,
			List<GameCard> faceUpCards,
			SuggestionResponse activeSuggestion,
			List<BonusCardRevealResponse> pendingBonusReveals,
			List<BonusCardRevealResponse> publicBonusReveals) {
		return from(game, activeTurn, weaponTokens, game.getCharacterTokens(), myHand, myBonusCards, faceUpCards,
				activeSuggestion, pendingBonusReveals, publicBonusReveals);
	}

	public static GameStateResponse from(
			Game game,
			Turn activeTurn,
			List<WeaponToken> weaponTokens,
			List<CharacterToken> characterTokens,
			List<GameCard> myHand,
			List<BonusCard> myBonusCards,
			List<GameCard> faceUpCards,
			SuggestionResponse activeSuggestion,
			List<BonusCardRevealResponse> pendingBonusReveals,
			List<BonusCardRevealResponse> publicBonusReveals) {
		return new GameStateResponse(
				game.getId(),
				game.getName(),
				game.getCode(),
				game.getMaxPlayers(),
				game.getStatus(),
				game.getHost(),
				game.getPlayers(),
				game.getBoard(),
				activeTurn,
				weaponTokens,
				characterTokens != null ? characterTokens : List.of(),
				myHand,
				myBonusCards.stream().map(BonusCardResponse::from).toList(),
				faceUpCards,
				game.getSolution() != null,
				game.getWinner(),
				activeSuggestion,
				pendingBonusReveals,
				publicBonusReveals,
				game.getStatus() == GameStatus.ENDED && game.getSolution() != null
						? GameSolutionResponse.from(game.getSolution())
						: null);
	}
}
