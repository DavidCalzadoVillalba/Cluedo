package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.card.CardService;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidGameCodeException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;

@Service
public class BonusCardRevealService {

	private static final int HASH_PREFIX_LENGTH = 1;

	private final GameRepository gameRepository;
	private final PlayerRepository playerRepository;
	private final UserService userService;
	private final CardService cardService;

	public BonusCardRevealService(
			GameRepository gameRepository,
			PlayerRepository playerRepository,
			UserService userService,
			CardService cardService) {
		this.gameRepository = gameRepository;
		this.playerRepository = playerRepository;
		this.userService = userService;
		this.cardService = cardService;
	}

	@Transactional
	public BonusCardRevealResponse reveal(String code, Integer revealId, Integer cardId) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		Player player = playerRepository.findByGameAndUser(game, userService.findCurrentUser())
				.orElseThrow(() -> new AccessDeniedException("No perteneces a esta partida"));
		return BonusCardRevealResponse.from(
				cardService.revealCardToEveryone(revealId, game, player, cardId));
	}

	private String normalizeCode(String code) {
		if (code == null || code.isBlank()) {
			throw new InvalidGameCodeException();
		}
		String normalized = code.trim().toUpperCase();
		return normalized.startsWith("#") ? normalized.substring(HASH_PREFIX_LENGTH) : normalized;
	}
}
