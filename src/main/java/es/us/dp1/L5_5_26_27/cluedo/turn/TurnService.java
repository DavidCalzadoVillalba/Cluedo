package es.us.dp1.L5_5_26_27.cluedo.turn;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.card.CardService;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardResponse;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.DiceAlreadyRolledException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.DiceNotRolledException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameNotStartedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidGameCodeException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.TurnAlreadyMovedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.TurnExpiredException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.TurnNotActiveException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.SecretPassageNotAllowedException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;

@Service
public class TurnService {

	private static final Duration TURN_DURATION = Duration.ofSeconds(60);

	private final TurnRepository turnRepository;
	private final PlayerRepository playerRepository;
	private final GameRepository gameRepository;
	private final UserService userService;
	private final BoardService boardService;
	private final CardService cardService;
	private final Random random = new Random();

	public TurnService(
			TurnRepository turnRepository,
			PlayerRepository playerRepository,
			GameRepository gameRepository,
			UserService userService,
			BoardService boardService,
			CardService cardService) {
		this.turnRepository = turnRepository;
		this.playerRepository = playerRepository;
		this.gameRepository = gameRepository;
		this.userService = userService;
		this.boardService = boardService;
		this.cardService = cardService;
	}

	@Transactional
	public InitialRollResult initialRoll(String code) {
		Game game = findStartedGame(code);
		Player player = findCurrentPlayer(game);
		if (player.getInitialDiceResult() != null) {
			throw new GameRuleException("Ya has realizado la tirada inicial");
		}

		int[] dice = rollDice();
		player.setInitialDiceOne(dice[0]);
		player.setInitialDiceTwo(dice[1]);
		int result = dice[0] + dice[1];
		player.setInitialDiceResult(result);
		playerRepository.save(player);

		List<Player> players = playerRepository.findByGameOrderByIdAsc(game);
		boolean orderDetermined = players.stream().allMatch(candidate -> candidate.getInitialDiceResult() != null);
		if (orderDetermined) {
			resolveTies(players);
			Player startingPlayer = players.stream()
					.max(Comparator.comparing(Player::getInitialDiceResult)
							.thenComparing(Player::getTurnOrder))
					.orElseThrow(() -> new GameRuleException("No hay jugadores para iniciar el turno"));
			int playerCount = players.size();
			int startingOrder = startingPlayer.getTurnOrder();
			players.sort(Comparator.comparingInt(candidate -> {
				int distance = candidate.getTurnOrder() - startingOrder;
				return distance < 0 ? distance + playerCount : distance;
			}));
			for (int i = 0; i < players.size(); i++) {
				players.get(i).setTurnOrder(i + 1);
			}
			playerRepository.saveAll(players);
			createTurn(game, startingPlayer, 1);
		}

		return new InitialRollResult(player.getId(), result, orderDetermined, toPlayerRolls(players));
	}

	@Transactional
	public Turn roll(Integer turnId) {
		Turn turn = findTurn(turnId);
		validateAction(turn);
		if (turn.getDiceResult() != null) {
			throw new DiceAlreadyRolledException("Ya has tirado los dados en este turno");
		}
		if (turn.isHasMoved()) {
			throw new TurnAlreadyMovedException();
		}
		int[] dice = rollDice();
		turn.setDiceOne(dice[0]);
		turn.setDiceTwo(dice[1]);
		turn.setDiceResult(dice[0] + dice[1] + turn.getMovementBonus());
		return turnRepository.save(turn);
	}

	@Transactional(readOnly = true)
	public List<Box> getReachableBoxes(Integer turnId) {
		Turn turn = findTurn(turnId);
		validateAction(turn);
		if (turn.getDiceResult() == null) {
			throw new DiceNotRolledException("Debes tirar los dados antes de consultar las casillas alcanzables");
		}
		if (turn.isHasMoved()) {
			return List.of();
		}
		return boardService.getReachableBoxes(turn.getPlayer(), turn.getDiceResult());
	}

	@Transactional
	public TurnActionResponse move(Integer turnId, Integer targetBoxId) {
		Turn turn = findTurn(turnId);
		validateAction(turn);
		if (turn.getDiceResult() == null) {
			throw new DiceNotRolledException("Debes tirar los dados antes de moverte");
		}
		if (turn.isHasMoved()) {
			throw new TurnAlreadyMovedException();
		}
		Box destination = boardService.movePlayer(turn.getPlayer(), targetBoxId, turn.getDiceResult());
		turn.setHasMoved(true);
		boolean landedOnBonus = BoardLayout.isBonusCell(destination.getX(), destination.getY());
		BonusCard drawnCard = landedOnBonus
				? cardService.drawBonusCard(turn.getGame(), turn.getPlayer())
				: null;
		Turn savedTurn = turnRepository.save(turn);
		return new TurnActionResponse(savedTurn, BonusCardResponse.from(drawnCard), landedOnBonus);
	}

	@Transactional
	public BonusCardResponse useBonusCard(Integer turnId, Integer cardId, RoomType room) {
		return useBonusCard(turnId, cardId, room, null);
	}

	@Transactional
	public BonusCardResponse useBonusCard(Integer turnId, Integer cardId, RoomType room, Integer targetPlayerId) {
		Turn turn = findTurn(turnId);
		validateAction(turn);
		BonusCard card = cardService.findBonusCardForPlayer(cardId, turn.getPlayer());

		switch (card.getEffect()) {
			case EXTRA_DICE_6 -> {
				if (turn.isHasMoved()) {
					turn.setDiceResult(6);
					turn.setHasMoved(false);
				} else {
					turn.setMovementBonus(turn.getMovementBonus() + 6);
					if (turn.getDiceResult() != null) {
						turn.setDiceResult(turn.getDiceResult() + 6);
					}
				}
			}
			case TELEPORT_ANY_ROOM -> {
				if (!card.isImmediate() && (turn.isHasMoved() || turn.getDiceResult() != null)) {
					throw new GameRuleException(
							"La carta de teletransporte debe usarse antes de tirar los dados");
				}
				if (room == null || room == RoomType.CENTER) {
					throw new GameRuleException("Elige una habitación válida para teletransportarte");
				}
				boardService.teleportPlayerToRoom(turn.getPlayer(), room);
				turn.setHasMoved(true);
			}
			case EXTRA_TURN -> turn.setExtraTurn(true);
			case REVEAL_CARD -> cardService.createPublicReveal(turn.getGame(), turn.getPlayer(), targetPlayerId, card);
			case PEEK_CARD, EXTRA_QUESTION -> throw new GameRuleException(
					"Esta carta debe usarse durante su acción específica");
			case SKIP_REFUTATION -> throw new GameRuleException(
					"La carta solo se puede usar durante una fase de refutación");
		}

		cardService.discardBonusCard(card);
		turnRepository.save(turn);
		return BonusCardResponse.from(card);
	}

	@Transactional
	public Turn useSecretPassage(Integer turnId) {
		Turn turn = findTurn(turnId);
		validateAction(turn);
		if (turn.isHasMoved()) {
			throw new TurnAlreadyMovedException();
		}
		if (turn.getDiceResult() != null) {
			throw new SecretPassageNotAllowedException();
		}
		boardService.useSecretPassage(turn.getPlayer());
		turn.setHasMoved(true);
		return turnRepository.save(turn);
	}

	@Transactional
	public Turn finishTurn(Integer turnId) {
		Turn turn = findTurn(turnId);
		validateAction(turn);
		return advanceTurn(turn, TurnStatus.FINISHED);
	}

	@Transactional(readOnly = true)
	public Turn findTurn(Integer turnId) {
		return turnRepository.findById(turnId)
				.orElseThrow(() -> new ResourceNotFoundException("Turno", "id", turnId));
	}

	@Transactional(readOnly = true)
	public void validateAction(Turn turn) {
		if (turn.getStatus() != TurnStatus.ACTIVE) {
			throw new TurnNotActiveException();
		}
		User current = userService.findCurrentUser();
		if (!turn.getPlayer().getUser().getId().equals(current.getId())) {
			throw new AccessDeniedException("No es tu turno");
		}
		if (turn.getExpiresAt() == null || !turn.getExpiresAt().isAfter(Instant.now())) {
			throw new TurnExpiredException();
		}
	}

	@Scheduled(fixedDelay = 1000)
	@Transactional
	public void finishExpiredTurns() {
		for (Turn turn : turnRepository.findByStatusAndExpiresAtLessThanEqual(TurnStatus.ACTIVE, Instant.now())) {
			advanceTurn(turn, TurnStatus.TIMEOUT);
		}
	}

	private Turn advanceTurn(Turn current, TurnStatus finalStatus) {
		current.setStatus(finalStatus);
		turnRepository.save(current);

		List<Player> players = playerRepository.findByGameOrderByIdAsc(current.getGame()).stream()
				.filter(player -> !player.isEliminated() && player.getTurnOrder() != null)
				.sorted(Comparator.comparing(Player::getTurnOrder))
				.toList();
		if (players.isEmpty()) {
			current.getGame().setStatus(GameStatus.ENDED);
			current.getGame().setEndedAt(Instant.now());
			gameRepository.save(current.getGame());
			return current;
		}
		int currentIndex = -1;
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getId() != null && players.get(i).getId().equals(current.getPlayer().getId())) {
				currentIndex = i;
				break;
			}
		}
		Player next;
		if (current.isExtraTurn() && !current.getPlayer().isEliminated()) {
			next = current.getPlayer();
		} else {
			int nextIndex = currentIndex >= 0 ? (currentIndex + 1) % players.size() : 0;
			next = players.get(nextIndex);
		}
		return createTurn(current.getGame(), next, current.getTurnNumber() + 1);
	}

	private Turn createTurn(Game game, Player player, int turnNumber) {
		Instant start = Instant.now();
		Turn turn = new Turn();
		turn.setGame(game);
		turn.setPlayer(player);
		turn.setTurnNumber(turnNumber);
		turn.setStatus(TurnStatus.ACTIVE);
		turn.setHasMoved(false);
		turn.setStartedAt(start);
		turn.setExpiresAt(start.plus(TURN_DURATION));
		return turnRepository.save(turn);
	}

	private void resolveTies(List<Player> players) {
		int maxScore = players.stream()
				.mapToInt(Player::getInitialDiceResult)
				.max()
				.orElse(0);
		List<Player> tiedPlayers = players.stream()
				.filter(p -> p.getInitialDiceResult() == maxScore)
				.toList();
		if (tiedPlayers.size() <= 1) {
			return;
		}

		List<Player> contenders = new ArrayList<>(tiedPlayers);
		while (contenders.size() > 1) {
			for (Player contender : contenders) {
				int[] dice = rollDice();
				contender.setInitialDiceOne(dice[0]);
				contender.setInitialDiceTwo(dice[1]);
				contender.setInitialDiceResult(dice[0] + dice[1]);
			}
			int roundMax = contenders.stream()
					.mapToInt(Player::getInitialDiceResult)
					.max()
					.orElse(0);
			contenders = contenders.stream()
					.filter(p -> p.getInitialDiceResult() == roundMax)
					.toList();
		}

		Player winner = contenders.get(0);
		int otherMax = players.stream()
				.filter(p -> !p.getId().equals(winner.getId()))
				.mapToInt(Player::getInitialDiceResult)
				.max()
				.orElse(0);
		while (winner.getInitialDiceResult() <= otherMax) {
			int[] dice = rollDice();
			winner.setInitialDiceOne(dice[0]);
			winner.setInitialDiceTwo(dice[1]);
			winner.setInitialDiceResult(dice[0] + dice[1]);
			if (winner.getInitialDiceResult() > otherMax) {
				break;
			}
			if (otherMax >= 12) {
				winner.setInitialDiceResult(12);
				winner.setInitialDiceOne(6);
				winner.setInitialDiceTwo(6);
				break;
			}
		}
	}

	private List<PlayerRoll> toPlayerRolls(List<Player> players) {
		return players.stream()
				.map(player -> new PlayerRoll(player.getId(), player.getInitialDiceResult(), player.getTurnOrder()))
				.toList();
	}

	private Game findStartedGame(String code) {
		if (code == null || code.isBlank()) {
			throw new InvalidGameCodeException();
		}
		String normalized = code.trim().toUpperCase();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		final String gameCode = normalized;
		Game game = gameRepository.findByCode(gameCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", gameCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida todavía no ha comenzado");
		}
		return game;
	}

	private Player findCurrentPlayer(Game game) {
		User current = userService.findCurrentUser();
		return playerRepository.findByGameAndUser(game, current)
				.orElseThrow(() -> new AccessDeniedException("No perteneces a esta partida"));
	}

	private int[] rollDice() {
		return new int[] { random.nextInt(6) + 1, random.nextInt(6) + 1 };
	}
}
