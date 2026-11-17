package es.us.dp1.L5_5_26_27.cluedo.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.BoxType;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameNotStartedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.NoActiveTurnException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnStatus;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;

@ExtendWith(MockitoExtension.class)
class BoardServiceTests {

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private PlayerRepository playerRepository;

	@Mock
	private GameRepository gameRepository;

	@Mock
	private UserService userService;

	@Mock
	private TurnRepository turnRepository;

	@InjectMocks
	private BoardService boardService;

	private Game game;
	private Board board;
	private Box[][] grid;
	private List<Box> allBoxes;

	@BeforeEach
	void setUp() {
		game = new Game();
		game.setId(1);
		game.setCode("TEST01");

		board = new Board();
		board.setId(1);
		board.setWidth(BoardLayout.WIDTH);
		board.setHeight(BoardLayout.HEIGHT);
		board.setGame(game);
		game.setBoard(board);

		grid = new Box[BoardLayout.HEIGHT][BoardLayout.WIDTH];
		allBoxes = new ArrayList<>();

		int idSequence = 1;
		for (int y = 0; y < BoardLayout.HEIGHT; y++) {
			for (int x = 0; x < BoardLayout.WIDTH; x++) {
				Box box = new Box();
				box.setId(idSequence++);
				box.setBoard(board);
				box.setX(x);
				box.setY(y);
				box.setType(BoxType.PATH);
				box.setRoom(null);

				grid[y][x] = box;
				allBoxes.add(box);
			}
		}
		board.setBoxes(allBoxes);
	}

	private Player createPlayer(int id, CharacterType character, Box box) {
		Player player = new Player();
		player.setId(id);
		player.setCharacter(character);
		player.setCurrentBox(box);
		player.setGame(game);
		return player;
	}

	private User authenticate(Player player) {
		User user = new User();
		user.setId(player.getId());
		user.setUsername("player" + player.getId());
		player.setUser(user);
		game.setPlayers(List.of(player));
		when(userService.findCurrentUser()).thenReturn(user);
		return user;
	}

	@Test
	@DisplayName("1. Llegar a una casilla de tipo ROOM desde un PATH pasando por una DOOR")
	void shouldReachRoomFromPathPassingThroughDoor() {
		// Configuramos una habitación con su puerta y casillas interiores
		Box kitchenDoor = grid[5][14];
		kitchenDoor.setType(BoxType.DOOR);
		kitchenDoor.setRoom(RoomType.KITCHEN);

		Box kitchenInterior = grid[3][17];
		kitchenInterior.setType(BoxType.ROOM);
		kitchenInterior.setRoom(RoomType.KITCHEN);

		// El jugador empieza en el pasillo adyacente (13, 5)
		Box startHallway = grid[5][13];
		Player player = createPlayer(1, CharacterType.RUBIO, startHallway);
		game.setPlayers(List.of(player));

		// Con tirada de 1 dado, puede acceder a la puerta y por tanto a la cocina
		List<Box> reachable = boardService.getReachableBoxes(player, 1);

		assertTrue(reachable.contains(kitchenDoor), "La puerta de la cocina debe ser alcanzable");
		assertTrue(reachable.contains(kitchenInterior), "Las casillas de la cocina deben ser alcanzables");
	}

	@Test
	@DisplayName("Solo las cinco coordenadas configuradas son casillas bonus")
	void shouldMarkOnlyConfiguredCellsAsBonus() {
		game.setPlayers(List.of());
		Board createdBoard = boardService.createForGame(game);

		List<BoardLayout.Coord> expectedBonusCells = List.of(
				new BoardLayout.Coord(10, 5),
				new BoardLayout.Coord(7, 7),
				new BoardLayout.Coord(9, 12),
				new BoardLayout.Coord(11, 14),
				new BoardLayout.Coord(12, 8));

		assertTrue(expectedBonusCells.stream()
				.allMatch(
						coord -> boardService.findBox(createdBoard, coord.x(), coord.y()).getType() == BoxType.BONUS));
		assertTrue(createdBoard.getBoxes().stream()
				.filter(box -> box.getType() == BoxType.BONUS)
				.allMatch(box -> expectedBonusCells.contains(new BoardLayout.Coord(box.getX(), box.getY()))));
		assertFalse(boardService.findBox(createdBoard, 8, 4).getType() == BoxType.BONUS);
		assertFalse(boardService.findBox(createdBoard, 12, 11).getType() == BoxType.BONUS);
	}

	@Test
	@DisplayName("2. Comprobar que un camino queda descartado si en medio hay otro jugador bloqueando la casilla de PATH (RN-11)")
	void shouldBlockPathWhenAnotherPlayerOccupiesIntermediateCell() {
		// Pasillo en línea recta: (10, 6) -> (10, 5) -> (10, 4)
		Box startBox = grid[6][10];
		Box blockedBox = grid[5][10];
		Box targetBox = grid[4][10];

		Player player1 = createPlayer(1, CharacterType.RUBIO, startBox);
		Player player2 = createPlayer(2, CharacterType.CELESTE, blockedBox); // Bloquea la casilla intermedia

		game.setPlayers(List.of(player1, player2));

		// Tirada de 2 pasos
		List<Box> reachable = boardService.getReachableBoxes(player1, 2);

		assertFalse(reachable.contains(blockedBox), "La casilla ocupada por otro jugador no puede ser destino");
		// Si (10, 5) está bloqueada, el camino hacia (10, 4) por esa ruta debe quedar
		// descartado
		// Verificamos que la casilla bloqueada no está en los alcanzables
		assertFalse(reachable.stream().anyMatch(b -> b.getId().equals(blockedBox.getId())),
				"La casilla ocupada debe estar descartada");
	}

	@Test
	@DisplayName("3. Comprobar que un jugador que empieza en una ROOM no puede volver a entrar en esa misma habitación (RN-10)")
	void shouldNotAllowReenteringSameRoomWherePlayerStarted() {
		// Configuramos el Dormitorio con su puerta
		Box bedroomDoor = grid[4][5];
		bedroomDoor.setType(BoxType.DOOR);
		bedroomDoor.setRoom(RoomType.BEDROOM);

		Box bedroomCenter = grid[3][2];
		bedroomCenter.setType(BoxType.ROOM);
		bedroomCenter.setRoom(RoomType.BEDROOM);

		Box bedroomOtherCell = grid[2][2];
		bedroomOtherCell.setType(BoxType.ROOM);
		bedroomOtherCell.setRoom(RoomType.BEDROOM);

		// Jugador empieza dentro del dormitorio
		Player player = createPlayer(1, CharacterType.AMAPOLA, bedroomCenter);
		game.setPlayers(List.of(player));

		// Tirada de 3 pasos
		List<Box> reachable = boardService.getReachableBoxes(player, 3);

		// RN-10: Ninguna casilla del dormitorio de inicio puede estar entre las
		// alcanzables
		boolean containsBedroomCells = reachable.stream()
				.anyMatch(b -> b.getRoom() == RoomType.BEDROOM);

		assertFalse(containsBedroomCells,
				"El jugador no debe poder moverse dentro ni volver a entrar al dormitorio en la misma tirada");
	}

	@Test
	@DisplayName("4. Salir del CENTRO con tirada 5 permite alcanzar el COMEDOR a través de la puerta este")
	void shouldReachDiningRoomFromCenterWithDiceRollFive() {
		// Puertas del centro
		Box centerEastDoor = grid[10][11]; // (11, 10)
		centerEastDoor.setType(BoxType.DOOR);
		centerEastDoor.setRoom(RoomType.CENTER);

		Box centerBox = grid[10][10]; // Centro (10, 10)
		centerBox.setType(BoxType.ROOM);
		centerBox.setRoom(RoomType.CENTER);

		// Puerta del comedor en (13, 9)
		Box diningDoor = grid[9][13];
		diningDoor.setType(BoxType.DOOR);
		diningDoor.setRoom(RoomType.DINING_ROOM);

		Box diningInterior = grid[9][14];
		diningInterior.setType(BoxType.ROOM);
		diningInterior.setRoom(RoomType.DINING_ROOM);

		Player player = createPlayer(1, CharacterType.CELESTE, centerBox);
		game.setPlayers(List.of(player));

		// Tirada de 5 dados desde el centro
		List<Box> reachable = boardService.getReachableBoxes(player, 5);

		// Paso 1: (12, 10), Paso 2: (12, 9), Paso 3: (13, 9) puerta comedor -> COMEDOR
		// alcanzable
		assertTrue(reachable.contains(diningInterior),
				"El comedor debe ser alcanzable desde el centro con un 5");
		assertTrue(reachable.contains(diningDoor),
				"La puerta del comedor debe ser alcanzable desde el centro con un 5");
	}

	@Test
	@DisplayName("5. Con tirada 5, permite pararse en cualquier casilla intermedia del camino (ej. bonus a 3 pasos)")
	void shouldReachIntermediateCellsIncludingBonusWithinDiceRoll() {
		Box startBox = grid[6][10];
		Box step1 = grid[7][10];
		Box step2 = grid[8][10];
		Box bonusStep3 = grid[9][10];
		bonusStep3.setType(BoxType.BONUS);

		Player player = createPlayer(1, CharacterType.RUBIO, startBox);
		game.setPlayers(List.of(player));

		List<Box> reachable = boardService.getReachableBoxes(player, 5);

		assertTrue(reachable.contains(step1), "La casilla a 1 paso debe ser alcanzable");
		assertTrue(reachable.contains(step2), "La casilla a 2 pasos debe ser alcanzable");
		assertTrue(reachable.contains(bonusStep3), "La casilla bonus a 3 pasos debe ser alcanzable");
	}

	@Test
	@DisplayName("Resuelve la partida por id y calcula el movimiento del jugador autenticado")
	void shouldResolveGameByIdAndCurrentPlayer() {
		Box start = grid[6][10];
		Player player = createPlayer(1, CharacterType.RUBIO, start);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);

		Turn turn = new Turn();
		turn.setPlayer(player);
		turn.setDiceResult(1);
		turn.setStatus(TurnStatus.ACTIVE);

		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));

		List<Box> reachable = boardService.getReachableBoxes("1", 1);

		assertTrue(reachable.contains(grid[7][10]));
	}

	@Test
	@DisplayName("Resuelve la partida por código y mueve al jugador autenticado")
	void shouldResolveGameByCodeAndMoveCurrentPlayer() {
		Box start = grid[6][10];
		Box target = grid[7][10];
		Player player = createPlayer(1, CharacterType.RUBIO, start);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);

		Turn turn = new Turn();
		turn.setPlayer(player);
		turn.setDiceResult(1);
		turn.setStatus(TurnStatus.ACTIVE);

		when(gameRepository.findByCode("TEST01")).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));

		Box moved = boardService.movePlayer("TEST01", target.getId(), 1);

		assertEquals(target, moved);
		assertEquals(target, player.getCurrentBox());
		assertTrue(turn.isHasMoved());
	}

	@Test
	@DisplayName("Rechaza un identificador de partida vacío")
	void shouldRejectBlankGameId() {
		GameRuleException exception = assertThrows(GameRuleException.class,
				() -> boardService.getReachableBoxes("  ", 1));

		assertEquals("Identificador de partida no válido", exception.getMessage());
	}

	@Test
	@DisplayName("Rechaza con ResourceNotFoundException una partida que no existe ni por id ni por código")
	void shouldRejectUnknownGame() {
		when(gameRepository.findById(99)).thenReturn(Optional.empty());
		when(gameRepository.findByCode("99")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> boardService.getReachableBoxes("99", 1));
	}

	@Test
	@DisplayName("Rechaza con AccessDeniedException al usuario autenticado que no participa en la partida")
	void shouldRejectAuthenticatedUserWhoIsNotInTheGame() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		User other = new User();
		other.setId(8);
		player.setUser(other);
		game.setPlayers(List.of(player));

		User current = new User();
		current.setId(7);
		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(userService.findCurrentUser()).thenReturn(current);

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 1));

		assertEquals("El usuario autenticado no participa en esta partida", exception.getMessage());
	}

	@Test
	@DisplayName("Rechaza el movimiento si la partida no está en curso")
	void shouldRejectMovementWhenGameIsNotInProgress() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		authenticate(player);
		game.setStatus(GameStatus.WAITING);
		when(gameRepository.findById(1)).thenReturn(Optional.of(game));

		GameNotStartedException ex = assertThrows(GameNotStartedException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 1));

		assertEquals("La partida no está en curso", ex.getMessage());
	}

	@Test
	@DisplayName("Rechaza el movimiento si no hay un turno activo")
	void shouldRejectMovementWhenNoActiveTurnExists() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);
		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.empty());

		NoActiveTurnException ex = assertThrows(NoActiveTurnException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 1));

		assertEquals("No hay un turno activo en la partida", ex.getMessage());
	}

	@Test
	@DisplayName("Rechaza con AccessDeniedException si no es el turno del jugador")
	void shouldRejectMovementWhenNotCurrentUsersTurn() {
		Player player1 = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		Player player2 = createPlayer(2, CharacterType.CELESTE, grid[5][10]);
		authenticate(player1);
		game.setStatus(GameStatus.STARTED);

		Turn turnOfOtherPlayer = new Turn();
		turnOfOtherPlayer.setPlayer(player2);
		turnOfOtherPlayer.setDiceResult(2);
		turnOfOtherPlayer.setStatus(TurnStatus.ACTIVE);

		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turnOfOtherPlayer));

		AccessDeniedException ex = assertThrows(AccessDeniedException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 2));

		assertEquals("No es tu turno para moverte", ex.getMessage());
	}

	@Test
	@DisplayName("Rechaza el movimiento si el turno ha expirado")
	void shouldRejectMovementWhenTurnHasExpired() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);

		Turn turn = new Turn();
		turn.setPlayer(player);
		turn.setDiceResult(3);
		turn.setStatus(TurnStatus.ACTIVE);
		turn.setExpiresAt(Instant.now().minusSeconds(10));

		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 3));

		assertEquals("El tiempo del turno ha terminado", ex.getMessage());
	}

	@Test
	@DisplayName("Rechaza el movimiento si aún no se han tirado los dados en el turno")
	void shouldRejectMovementWhenDiceNotYetRolled() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);

		Turn turn = new Turn();
		turn.setPlayer(player);
		turn.setDiceResult(null);
		turn.setStatus(TurnStatus.ACTIVE);

		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 3));

		assertEquals("Debes tirar los dados antes de moverte", ex.getMessage());
	}

	@Test
	@DisplayName("Rechaza el movimiento si la tirada enviada no coincide con el resultado de los dados del turno")
	void shouldRejectMovementWhenDiceRollDoesNotMatchTurn() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);

		Turn turn = new Turn();
		turn.setPlayer(player);
		turn.setDiceResult(4);
		turn.setStatus(TurnStatus.ACTIVE);

		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 6));

		assertEquals("La tirada no coincide con los dados del turno", ex.getMessage());
	}

	@Test
	@DisplayName("Rechaza el movimiento si el jugador ya se ha movido en este turno")
	void shouldRejectMovementWhenPlayerHasAlreadyMoved() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);

		Turn turn = new Turn();
		turn.setPlayer(player);
		turn.setDiceResult(2);
		turn.setStatus(TurnStatus.ACTIVE);
		turn.setHasMoved(true);

		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> boardService.movePlayer("1", grid[7][10].getId(), 2));

		assertEquals("Ya te has movido en este turno", ex.getMessage());
	}

	@Test
	@DisplayName("Devuelve lista vacía de casillas alcanzables si el jugador ya se ha movido en este turno")
	void shouldReturnEmptyReachableBoxesWhenPlayerHasAlreadyMoved() {
		Player player = createPlayer(1, CharacterType.RUBIO, grid[6][10]);
		authenticate(player);
		game.setStatus(GameStatus.STARTED);

		Turn turn = new Turn();
		turn.setPlayer(player);
		turn.setDiceResult(2);
		turn.setStatus(TurnStatus.ACTIVE);
		turn.setHasMoved(true);

		when(gameRepository.findById(1)).thenReturn(Optional.of(game));
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));

		List<Box> reachable = boardService.getReachableBoxes("1", 2);

		assertTrue(reachable.isEmpty());
	}
}
