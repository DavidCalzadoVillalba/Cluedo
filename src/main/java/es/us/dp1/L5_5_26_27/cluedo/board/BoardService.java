package es.us.dp1.L5_5_26_27.cluedo.board;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout.Coord;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout.SecretPassage;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.character.CharacterTokenRepository;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.BoardNotInitializedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.DiceNotRolledException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameNotStartedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.NoActiveTurnException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.TurnAlreadyMovedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.TurnExpiredException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnStatus;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;

@Service
public class BoardService {

	private final BoardRepository boardRepository;
	private final PlayerRepository playerRepository;
	private final GameRepository gameRepository;
	private final UserService userService;
	private final TurnRepository turnRepository;
	private final BoardMovementCalculator movementCalculator;
	private final BoardInitializer boardInitializer;

	@Autowired(required = false)
	private CharacterTokenRepository characterTokenRepository;

	@Autowired
	public BoardService(BoardRepository boardRepository, PlayerRepository playerRepository,
			GameRepository gameRepository, UserService userService,
			TurnRepository turnRepository,
			BoardMovementCalculator movementCalculator, BoardInitializer boardInitializer) {
		this.boardRepository = boardRepository;
		this.playerRepository = playerRepository;
		this.gameRepository = gameRepository;
		this.userService = userService;
		this.turnRepository = turnRepository;
		this.movementCalculator = movementCalculator != null ? movementCalculator : new BoardMovementCalculator();
		this.boardInitializer = boardInitializer != null ? boardInitializer : new BoardInitializer(playerRepository);
	}

	public BoardService(BoardRepository boardRepository, PlayerRepository playerRepository,
			GameRepository gameRepository, UserService userService,
			TurnRepository turnRepository) {
		this(boardRepository, playerRepository, gameRepository, userService, turnRepository,
				new BoardMovementCalculator(), new BoardInitializer(playerRepository));
	}

	public BoardService(BoardRepository boardRepository, PlayerRepository playerRepository,
			GameRepository gameRepository, UserService userService) {
		this(boardRepository, playerRepository, gameRepository, userService, null,
				new BoardMovementCalculator(), new BoardInitializer(playerRepository));
	}

	@Transactional
	public Board createForGame(Game game) {
		Board board = new Board();
		board.setGame(game);
		board.setWidth(BoardLayout.WIDTH);
		board.setHeight(BoardLayout.HEIGHT);
		game.setBoard(board);

		Box[][] grid = boardInitializer.initializeBoard(board);
		boardRepository.save(board);

		boardInitializer.placePlayers(game, grid);
		return board;
	}

	@Transactional(readOnly = true)
	public Box findBox(Board board, int x, int y) {
		return board.getBoxes().stream()
				.filter(box -> box.getX() == x && box.getY() == y)
				.findFirst()
				.orElseThrow(() -> new GameRuleException("No existe la casilla (" + x + "," + y + ")"));
	}

	@Transactional(readOnly = true)
	public List<Box> getReachableBoxes(String gameId, int diceRoll) {
		Game game = findGameByIdOrCode(gameId);
		Player player = findCurrentPlayerInGame(game);
		Turn activeTurn = validateTurnForMovement(game, player, diceRoll, false);
		if (activeTurn != null && activeTurn.isHasMoved()) {
			return List.of();
		}
		return getReachableBoxes(player, diceRoll);
	}

	@Transactional(readOnly = true)
	public List<Box> getReachableBoxes(Player player, int diceRoll) {
		return movementCalculator.calculateReachableBoxes(player, diceRoll);
	}

	@Transactional
	public Box movePlayer(String gameId, Integer targetBoxId, int diceRoll) {
		Game game = findGameByIdOrCode(gameId);
		Player player = findCurrentPlayerInGame(game);
		Turn activeTurn = validateTurnForMovement(game, player, diceRoll, true);
		Box movedBox = movePlayer(player, targetBoxId, diceRoll);
		if (activeTurn != null) {
			activeTurn.setHasMoved(true);
			if (turnRepository != null) {
				turnRepository.save(activeTurn);
			}
		}
		return movedBox;
	}

	@Transactional
	public Box movePlayer(Player player, Integer targetBoxId, int diceRoll) {
		List<Box> reachable = getReachableBoxes(player, diceRoll);
		Box target = reachable.stream()
				.filter(b -> b.getId().equals(targetBoxId))
				.findFirst()
				.orElseThrow(() -> new GameRuleException("Casilla no alcanzable con la tirada actual"));

		if (BoardLayout.isUnclickable(target.getX(), target.getY())) {
			throw new GameRuleException("La casilla seleccionada no es interactuable");
		}

		if (target.getRoom() != null) {
			// Colocar al jugador directamente en el centro de la sala
			Coord center = BoardLayout.roomCenter(target.getRoom());
			Box centerBox = findBox(player.getGame().getBoard(), center.x(), center.y());
			player.setCurrentBox(centerBox);
			if (target.getRoom().isCardRoom()) {
				player.setLastSuggestionRoom(target.getRoom());
			} else {
				player.setLastSuggestionRoom(null);
			}
			playerRepository.save(player);
			syncCharacterToken(player, centerBox);
			return centerBox;
		}

		player.setCurrentBox(target);
		player.setLastSuggestionRoom(null);
		playerRepository.save(player);
		syncCharacterToken(player, target);
		return target;
	}

	@Transactional
	public Box useSecretPassage(Player player) {
		Box current = player.getCurrentBox();
		if (current == null) {
			throw new GameRuleException("El jugador no tiene casilla asignada");
		}
		RoomType currentRoom = current.getRoom();
		if (currentRoom == null) {
			throw new GameRuleException("No estás en ninguna habitación");
		}

		// RN-12: Buscar si la habitación actual tiene pasadizo secreto
		SecretPassage passage = BoardLayout.SECRET_PASSAGES.stream()
				.filter(p -> p.room() == currentRoom)
				.findFirst()
				.orElseThrow(() -> new GameRuleException(
						"La habitación " + currentRoom + " no tiene pasadizo secreto"));

		Board board = player.getGame().getBoard();
		Coord destination = passage.to();
		Box destBox = findBox(board, destination.x(), destination.y());

		player.setCurrentBox(destBox);
		player.setLastSuggestionRoom(destBox.getRoom());
		playerRepository.save(player);
		syncCharacterToken(player, destBox);
		return destBox;
	}

	@Transactional
	public Box teleportPlayerToRoom(Player player, RoomType room) {
		if (room == null || !room.isCardRoom()) {
			throw new GameRuleException("Debes elegir una habitación válida");
		}
		Board board = player.getGame().getBoard();
		if (board == null) {
			throw new BoardNotInitializedException();
		}

		Coord center = BoardLayout.roomCenter(room);
		Box destination = findBox(board, center.x(), center.y());
		player.setCurrentBox(destination);
		player.setLastSuggestionRoom(room);
		playerRepository.save(player);
		syncCharacterToken(player, destination);
		return destination;
	}

	private void syncCharacterToken(Player player, Box box) {
		if (characterTokenRepository != null && player != null && player.getGame() != null && player.getCharacter() != null) {
			characterTokenRepository.findByGameAndCharacter(player.getGame(), player.getCharacter()).ifPresent(token -> {
				token.setCurrentBox(box);
				characterTokenRepository.save(token);
			});
		}
	}

	private Game findGameByIdOrCode(String gameId) {
		if (gameId == null || gameId.isBlank()) {
			throw new GameRuleException("Identificador de partida no válido");
		}
		try {
			Integer id = Integer.parseInt(gameId);
			return gameRepository.findById(id)
					.orElseGet(() -> gameRepository.findByCode(gameId)
							.orElseThrow(() -> new ResourceNotFoundException("Partida", "id/código", gameId)));
		} catch (NumberFormatException e) {
			return gameRepository.findByCode(gameId)
					.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", gameId));
		}
	}

	private Player findCurrentPlayerInGame(Game game) {
		User currentUser = userService.findCurrentUser();
		if (currentUser == null) {
			throw new AccessDeniedException("No hay un usuario autenticado");
		}
		return game.getPlayers().stream()
				.filter(p -> p.getUser() != null && p.getUser().getId().equals(currentUser.getId()))
				.findFirst()
				.orElseThrow(() -> new AccessDeniedException(
						"El usuario autenticado no participa en esta partida"));
	}

	private Turn validateTurnForMovement(Game game, Player player, int diceRoll, boolean isMoveAction) {
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}
		if (turnRepository == null) {
			return null;
		}
		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(() -> new NoActiveTurnException("No hay un turno activo en la partida"));

		if (activeTurn.getPlayer() == null || !activeTurn.getPlayer().getId().equals(player.getId())) {
			throw new AccessDeniedException("No es tu turno para moverte");
		}

		if (activeTurn.getExpiresAt() != null && Instant.now().isAfter(activeTurn.getExpiresAt())) {
			throw new TurnExpiredException();
		}

		if (activeTurn.getDiceResult() == null) {
			throw new DiceNotRolledException(isMoveAction
					? "Debes tirar los dados antes de moverte"
					: "Debes tirar los dados antes de consultar las casillas alcanzables");
		}

		if (activeTurn.getDiceResult() != diceRoll) {
			throw new GameRuleException("La tirada no coincide con los dados del turno");
		}

		if (isMoveAction && activeTurn.isHasMoved()) {
			throw new TurnAlreadyMovedException();
		}

		return activeTurn;
	}
}
