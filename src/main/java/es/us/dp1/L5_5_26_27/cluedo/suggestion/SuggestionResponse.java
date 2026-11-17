package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

public record SuggestionResponse(
		Integer id,
		Integer turnId,
		RoomType room,
		CharacterType character,
		WeaponType weapon,
		SuggestionStatus status,
		Integer currentResponderId,
		String currentResponderName,
		Integer suggestedByPlayerId,
		String suggestedByPlayerName,
		GameCard shownCard,
		Integer refutedByPlayerId,
		String refutedByPlayerName,
		boolean peekAvailable) {

	public static SuggestionResponse from(Suggestion suggestion) {
		return from(suggestion, null);
	}

	public static SuggestionResponse from(Suggestion suggestion, Integer currentUserId) {
		if (suggestion == null) {
			return null;
		}
		Integer currentResponderId = suggestion.getCurrentResponder() != null
				? suggestion.getCurrentResponder().getId()
				: null;
		String currentResponderName = suggestion.getCurrentResponder() != null
				&& suggestion.getCurrentResponder().getUser() != null
						? suggestion.getCurrentResponder().getUser().getUsername()
						: null;
		Integer suggestedByPlayerId = suggestion.getTurn() != null
				&& suggestion.getTurn().getPlayer() != null
						? suggestion.getTurn().getPlayer().getId()
						: null;
		String suggestedByPlayerName = suggestion.getTurn() != null
				&& suggestion.getTurn().getPlayer() != null
				&& suggestion.getTurn().getPlayer().getUser() != null
						? suggestion.getTurn().getPlayer().getUser().getUsername()
						: null;

		boolean isSuggester = currentUserId != null
				&& suggestion.getTurn() != null
				&& suggestion.getTurn().getPlayer() != null
				&& suggestion.getTurn().getPlayer().getUser() != null
				&& suggestion.getTurn().getPlayer().getUser().getId().equals(currentUserId);

		boolean isRevealer = currentUserId != null
				&& suggestion.getCurrentResponder() != null
				&& suggestion.getCurrentResponder().getUser() != null
				&& suggestion.getCurrentResponder().getUser().getId().equals(currentUserId);

		boolean isPeekedBy = currentUserId != null
				&& suggestion.getPeekedBy() != null
				&& suggestion.getPeekedBy().getUser() != null
				&& suggestion.getPeekedBy().getUser().getId().equals(currentUserId);

		GameCard revealedCard = (isSuggester || isRevealer || isPeekedBy) ? suggestion.getShownCard() : null;

		Integer refutedByPlayerId = null;
		String refutedByPlayerName = null;
		if (suggestion.getStatus() == SuggestionStatus.REFUTED) {
			refutedByPlayerId = currentResponderId;
			refutedByPlayerName = currentResponderName;
		}

		return new SuggestionResponse(
				suggestion.getId(),
				suggestion.getTurn() != null ? suggestion.getTurn().getId() : null,
				suggestion.getRoom(),
				suggestion.getCharacter(),
				suggestion.getWeapon(),
				suggestion.getStatus(),
				currentResponderId,
				currentResponderName,
				suggestedByPlayerId,
				suggestedByPlayerName,
				revealedCard,
				refutedByPlayerId,
				refutedByPlayerName,
				suggestion.getStatus() == SuggestionStatus.REFUTED
						&& suggestion.getShownCard() != null
						&& suggestion.getPeekedBy() == null);
	}
}
