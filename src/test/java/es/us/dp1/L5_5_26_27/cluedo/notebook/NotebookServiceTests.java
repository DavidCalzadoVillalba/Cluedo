package es.us.dp1.L5_5_26_27.cluedo.notebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTests {

	@Mock
	private NotebookMarkRepository notebookMarkRepository;

	@Mock
	private GameRepository gameRepository;

	@Mock
	private PlayerRepository playerRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private NotebookService notebookService;

	@Test
	void shouldCreateOneBlankMarkPerCardAndPlayer() {
		Player first = new Player();
		Player second = new Player();
		Game game = new Game();
		game.setPlayers(List.of(first, second));

		notebookService.initializeForGame(game);

		ArgumentCaptor<List<NotebookMark>> captor = ArgumentCaptor.forClass(List.class);
		verify(notebookMarkRepository).saveAll(captor.capture());
		List<NotebookMark> marks = captor.getValue();
		int marksPerPlayer = CharacterType.values().length + WeaponType.values().length
				+ (int) List.of(RoomType.values()).stream().filter(RoomType::isCardRoom).count();
		assertEquals(2 * marksPerPlayer, marks.size());
		assertTrue(marks.stream().allMatch(mark -> mark.getValue() == NotebookMarkValue.BLANK));
		assertTrue(marks.stream().allMatch(mark -> mark.getPlayer() == first || mark.getPlayer() == second));
		assertEquals(CharacterType.values().length * 2,
				marks.stream().filter(mark -> mark.getCategory() == NotebookCategory.SUSPECT).count());
		assertEquals(WeaponType.values().length * 2,
				marks.stream().filter(mark -> mark.getCategory() == NotebookCategory.WEAPON).count());
		assertEquals(List.of(RoomType.values()).stream().filter(RoomType::isCardRoom).count() * 2,
				marks.stream().filter(mark -> mark.getCategory() == NotebookCategory.ROOM).count());
		for (Player player : List.of(first, second)) {
			assertEquals(CharacterType.values().length,
					marks.stream().filter(mark -> mark.getPlayer() == player && mark.getCharacter() != null).count());
			assertEquals(WeaponType.values().length,
					marks.stream().filter(mark -> mark.getPlayer() == player && mark.getWeapon() != null).count());
			assertEquals(RoomType.values().length - 1,
					marks.stream().filter(mark -> mark.getPlayer() == player && mark.getRoom() != null).count());
		}
	}

	@Test
	void shouldSaveNoMarksForGameWithoutPlayers() {
		Game game = new Game();
		game.setPlayers(new ArrayList<>());

		notebookService.initializeForGame(game);

		verify(notebookMarkRepository).saveAll(anyList());
	}

	@Test
	void shouldReadMarksForAuthenticatedPlayerInGame() {
		Game game = new Game();
		game.setCode("TEST01");
		Player player = new Player();
		User user = new User();
		NotebookMark mark = new NotebookMark();
		mark.setId(42);
		mark.setPlayer(player);
		mark.setCategory(NotebookCategory.SUSPECT);
		mark.setCharacter(CharacterType.PRADO);
		mark.setValue(NotebookMarkValue.QUESTION);
		when(gameRepository.findByCode("TEST01")).thenReturn(Optional.of(game));
		when(userService.findCurrentUser()).thenReturn(user);
		when(playerRepository.findByGameAndUser(game, user)).thenReturn(Optional.of(player));
		when(notebookMarkRepository.findByPlayerOrderByCategoryAscIdAsc(player)).thenReturn(List.of(mark));

		List<NotebookMarkResponse> result = notebookService.getMarks("TEST01");

		assertEquals(List.of(new NotebookMarkResponse(
				42, NotebookCategory.SUSPECT, CharacterType.PRADO, null, null, NotebookMarkValue.QUESTION)), result);
	}

	@Test
	void shouldUpdateOnlyMarkOwnedByAuthenticatedPlayer() {
		Game game = new Game();
		game.setCode("TEST01");
		Player player = new Player();
		User user = new User();
		NotebookMark mark = new NotebookMark();
		mark.setId(42);
		mark.setPlayer(player);
		mark.setCategory(NotebookCategory.SUSPECT);
		mark.setCharacter(CharacterType.PRADO);
		mark.setValue(NotebookMarkValue.BLANK);
		when(gameRepository.findByCode("TEST01")).thenReturn(Optional.of(game));
		when(userService.findCurrentUser()).thenReturn(user);
		when(playerRepository.findByGameAndUser(game, user)).thenReturn(Optional.of(player));
		when(notebookMarkRepository.findByIdAndPlayer(42, player)).thenReturn(Optional.of(mark));
		when(notebookMarkRepository.save(mark)).thenReturn(mark);

		NotebookMarkResponse result = notebookService.updateMark("TEST01", 42, NotebookMarkValue.CONFIRMED);

		assertEquals(NotebookMarkValue.CONFIRMED, mark.getValue());
		assertEquals(NotebookMarkValue.CONFIRMED, result.value());
		verify(notebookMarkRepository).findByIdAndPlayer(eq(42), eq(player));
	}

	@Test
	void shouldNotUpdateMarkOwnedByAnotherPlayer() {
		Game game = new Game();
		game.setCode("TEST01");
		Player player = new Player();
		User user = new User();
		when(gameRepository.findByCode("TEST01")).thenReturn(Optional.of(game));
		when(userService.findCurrentUser()).thenReturn(user);
		when(playerRepository.findByGameAndUser(game, user)).thenReturn(Optional.of(player));
		when(notebookMarkRepository.findByIdAndPlayer(42, player)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> notebookService.updateMark("TEST01", 42, NotebookMarkValue.X));
	}
}
