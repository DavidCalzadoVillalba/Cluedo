package es.us.dp1.L5_5_26_27.cluedo.accusation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.card.CardService;
import es.us.dp1.L5_5_26_27.cluedo.card.CharacterCard;
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
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnService;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnStatus;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Accusation Module")
@Feature("Final accusation")
@Owner("DP1-team")
@ExtendWith(MockitoExtension.class)
class AccusationServiceTests {

	@Mock
	private GameRepository gameRepository;
	@Mock
	private PlayerRepository playerRepository;
	@Mock
	private TurnRepository turnRepository;
	@Mock
	private SolutionRepository solutionRepository;
	@Mock
	private CardService cardService;
	@Mock
	private UserService userService;
	@Mock
	private TurnService turnService;

	@InjectMocks
	private AccusationService accusationService;

	private Game game;
	private Player player;
	private Turn turn;
	private User user;
	private List<GameCard> hand;

	@BeforeEach
	void setUp() {
		game = new Game();
		game.setCode("ABC123");
		game.setStatus(GameStatus.STARTED);
		user = new User();
		user.setId(5);
		player = new Player();
		player.setId(1);
		player.setUser(user);
		player.setGame(game);
		hand = new ArrayList<>();
		hand.add(new CharacterCard(CharacterType.AMAPOLA));
		player.setHand(hand);
		Box center = new Box();
		center.setRoom(RoomType.CENTER);
		player.setCurrentBox(center);
		turn = new Turn();
		turn.setId(10);
		turn.setPlayer(player);
		turn.setStatus(TurnStatus.ACTIVE);
		turn.setExpiresAt(Instant.now().plusSeconds(60));
		lenient().when(gameRepository.findByCode("ABC123")).thenReturn(Optional.of(game));
		lenient().when(userService.findCurrentUser()).thenReturn(user);
		lenient().when(playerRepository.findByGameAndUser(game, user)).thenReturn(Optional.of(player));
		lenient().when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.of(turn));
	}

	@Test
	void shouldEndGameAndSetWinnerForCorrectAccusation() {
		CharacterType character = CharacterType.values()[0];
		WeaponType weapon = WeaponType.values()[0];
		RoomType room = RoomType.KITCHEN;
		Solution solution = solution(character, weapon, room);
		when(solutionRepository.findByGame(game)).thenReturn(Optional.of(solution));

		AccusationResponse response = accusationService.accuse(" #abc123 ",
				new AccusationRequest(character, weapon, room));

		assertTrue(response.correct());
		assertEquals(GameStatus.ENDED, response.gameStatus());
		assertEquals(player, response.winner());
		assertTrue(response.revealedHiddenCards().isEmpty());
		assertEquals(GameSolutionResponse.from(solution), response.solution());
		assertEquals(player, game.getWinner());
		assertEquals(GameStatus.ENDED, game.getStatus());
		assertEquals(TurnStatus.FINISHED, turn.getStatus());
		assertFalse(player.isEliminated());
		assertSame(hand, player.getHand());
		verify(turnRepository).save(turn);
		verify(gameRepository).save(game);
		verify(playerRepository, never()).save(player);
		verify(turnService).validateAction(turn);
	}

	@Test
	void shouldEliminatePlayerAndRevealMatchingHiddenCardsForWrongAccusation() {
		CharacterType actualCharacter = CharacterType.values()[0];
		CharacterType accusedCharacter = CharacterType.values()[1];
		Solution solution = solution(actualCharacter, WeaponType.values()[0], RoomType.KITCHEN);
		when(solutionRepository.findByGame(game)).thenReturn(Optional.of(solution));
		CharacterCard hiddenMatch = new CharacterCard(accusedCharacter);
		when(cardService.getHiddenCardsMatching(game, accusedCharacter, WeaponType.values()[0], RoomType.KITCHEN))
				.thenReturn(List.of(hiddenMatch));

		AccusationResponse response = accusationService.accuse("ABC123",
				new AccusationRequest(accusedCharacter, WeaponType.values()[0], RoomType.KITCHEN));

		assertFalse(response.correct());
		assertEquals(GameStatus.STARTED, response.gameStatus());
		assertNull(response.winner());
		assertEquals(List.of(hiddenMatch), response.revealedHiddenCards());
		assertEquals(GameSolutionResponse.from(solution), response.solution());
		assertTrue(player.isEliminated());
		assertSame(hand, player.getHand());
		verify(playerRepository).save(player);
		verify(turnService).finishTurn(10);
		verify(gameRepository, never()).save(game);
	}

	@Test
	void shouldRejectAccusationWhenOnlyTwoSolutionCardsMatch() {
		Solution solution = solution(CharacterType.values()[0], WeaponType.values()[0], RoomType.KITCHEN);
		when(solutionRepository.findByGame(game)).thenReturn(Optional.of(solution));
		when(cardService.getHiddenCardsMatching(game, CharacterType.values()[0], WeaponType.values()[0], RoomType.LOUNGE))
				.thenReturn(List.of());

		AccusationResponse response = accusationService.accuse("ABC123",
				new AccusationRequest(CharacterType.values()[0], WeaponType.values()[0], RoomType.LOUNGE));

		assertFalse(response.correct());
		assertTrue(player.isEliminated());
		assertEquals(GameStatus.STARTED, game.getStatus());
	}

	@Test
	void shouldRejectAccusationFromOutsideCenterRoom() {
		Box hallway = new Box();
		player.setCurrentBox(hallway);

		AccusationOutsideCenterException exception = assertThrows(AccusationOutsideCenterException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));

		assertEquals("Solo puedes acusar desde la casilla central", exception.getMessage());
		verify(solutionRepository, never()).findByGame(game);
	}

	@Test
	void shouldRejectAccusationWhenThePlayerHasNoBox() {
		player.setCurrentBox(null);

		assertThrows(AccusationOutsideCenterException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));
	}

	@Test
	void shouldRejectAccusationFromAnEliminatedPlayer() {
		player.setEliminated(true);

		PlayerEliminatedException exception = assertThrows(PlayerEliminatedException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));

		assertEquals("El jugador eliminado no puede acusar", exception.getMessage());
		verify(turnRepository, never()).findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE);
	}

	@Test
	void shouldRejectAccusationWhenItIsNotThePlayersTurn() {
		Player other = new Player();
		other.setId(2);
		turn.setPlayer(other);

		NotYourTurnException exception = assertThrows(NotYourTurnException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));

		assertEquals("No es tu turno", exception.getMessage());
		verify(turnService, never()).validateAction(turn);
	}

	@Test
	void shouldRejectAccusationWhenTheGameHasNotStarted() {
		game.setStatus(GameStatus.WAITING);

		assertThrows(GameNotStartedException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));
		verify(userService, never()).findCurrentUser();
	}

	@Test
	void shouldRejectAccusationWhenTheGameDoesNotExist() {
		when(gameRepository.findByCode("ABC123")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));
	}

	@Test
	void shouldRejectABlankGameCode() {
		assertThrows(InvalidGameCodeException.class,
				() -> accusationService.accuse("  ", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));
		verify(gameRepository, never()).findByCode("ABC123");
	}

	@Test
	void shouldRejectAccusationWhenThePlayerIsNotInTheGame() {
		when(playerRepository.findByGameAndUser(game, user)).thenReturn(Optional.empty());

		assertThrows(PlayerNotInGameException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));
	}

	@Test
	void shouldRejectAccusationWhenThereIsNoActiveTurn() {
		when(turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThrows(NoActiveTurnException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));
	}

	@Test
	void shouldRejectAccusationWhenTheSolutionDoesNotExist() {
		when(solutionRepository.findByGame(game)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> accusationService.accuse("ABC123", new AccusationRequest(CharacterType.values()[0],
						WeaponType.values()[0], RoomType.KITCHEN)));
		verify(gameRepository, never()).save(game);
	}

	private Solution solution(CharacterType character, WeaponType weapon, RoomType room) {
		Solution solution = new Solution();
		solution.setCharacterSolution(character);
		solution.setWeaponSolution(weapon);
		solution.setRoomSolution(room);
		return solution;
	}
}
