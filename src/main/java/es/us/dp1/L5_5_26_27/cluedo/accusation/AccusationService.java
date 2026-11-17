package es.us.dp1.L5_5_26_27.cluedo.accusation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout;
import es.us.dp1.L5_5_26_27.cluedo.card.CardService;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.Solution;
import es.us.dp1.L5_5_26_27.cluedo.card.SolutionRepository;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccusationOutsideCenterException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameNotStartedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidGameCodeException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.NoActiveTurnException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.NotYourTurnException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.PlayerEliminatedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.PlayerNotInGameException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.game.GameSolutionResponse;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnService;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnStatus;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;

@Service
public class AccusationService {

	private final GameRepository gameRepository;
	private final PlayerRepository playerRepository;
	private final TurnRepository turnRepository;
	private final SolutionRepository solutionRepository;
	private final CardService cardService;
	private final UserService userService;
	private final TurnService turnService;

	public AccusationService(GameRepository gameRepository, PlayerRepository playerRepository,
			TurnRepository turnRepository, SolutionRepository solutionRepository, CardService cardService,
			UserService userService, TurnService turnService) {
		this.gameRepository = gameRepository;
		this.playerRepository = playerRepository;
		this.turnRepository = turnRepository;
		this.solutionRepository = solutionRepository;
		this.cardService = cardService;
		this.userService = userService;
		this.turnService = turnService;
	}

	@Transactional
	public AccusationResponse accuse(String code, AccusationRequest request) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}

		User currentUser = userService.findCurrentUser();
		Player player = playerRepository.findByGameAndUser(game, currentUser)
				.orElseThrow(PlayerNotInGameException::new);
		if (player.isEliminated()) {
			throw new PlayerEliminatedException("El jugador eliminado no puede acusar");
		}

		Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(NoActiveTurnException::new);
		if (!turn.getPlayer().getId().equals(player.getId())) {
			throw new NotYourTurnException("No es tu turno");
		}
		turnService.validateAction(turn);
		if (!BoardLayout.isInAccusationRoom(player.getCurrentBox())) {
			throw new AccusationOutsideCenterException();
		}

		Solution solution = solutionRepository.findByGame(game)
				.orElseThrow(() -> new ResourceNotFoundException("Solución", "partida", game.getCode()));
		boolean correct = solution.getCharacterSolution() == request.character()
				&& solution.getWeaponSolution() == request.weapon()
				&& solution.getRoomSolution() == request.room();

		if (correct) {
			player.setEliminated(false);
			game.setWinner(player);
			game.setStatus(GameStatus.ENDED);
			game.setEndedAt(java.time.Instant.now());
			turn.setStatus(TurnStatus.FINISHED);
			turnRepository.save(turn);
			gameRepository.save(game);
			return new AccusationResponse(true, game.getStatus(), player, List.of(),
					GameSolutionResponse.from(solution));
		}

		List<GameCard> revealed = cardService.getHiddenCardsMatching(game, request.character(), request.weapon(),
				request.room());
		player.setEliminated(true);
		playerRepository.save(player);
		turnService.finishTurn(turn.getId());
		return new AccusationResponse(false, game.getStatus(), null, revealed,
				GameSolutionResponse.from(solution));
	}

	private String normalizeCode(String code) {
		if (code == null || code.isBlank()) {
			throw new InvalidGameCodeException();
		}
		String normalized = code.trim().toUpperCase();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		return normalized;
	}
}
