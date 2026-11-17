package es.us.dp1.L5_5_26_27.cluedo.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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

import es.us.dp1.L5_5_26_27.cluedo.board.Board;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;

@ExtendWith(MockitoExtension.class)
class WeaponTokenServiceTests {

	@Mock
	private WeaponTokenRepository weaponTokenRepository;

	@Mock
	private BoardService boardService;

	@InjectMocks
	private WeaponTokenService weaponTokenService;

	@Test
	void shouldCreateOneTokenPerWeaponInItsStartingRoom() {
		Game game = new Game();
		Board board = new Board();
		game.setBoard(board);
		List<Box> startingBoxes = new ArrayList<>();
		when(boardService.findBox(any(Board.class), org.mockito.ArgumentMatchers.anyInt(),
				org.mockito.ArgumentMatchers.anyInt())).thenAnswer(invocation -> {
					Box box = new Box();
					startingBoxes.add(box);
					return box;
				});

		weaponTokenService.createForGame(game);

		ArgumentCaptor<List<WeaponToken>> captor = ArgumentCaptor.forClass(List.class);
		verify(weaponTokenRepository).saveAll(captor.capture());
		List<WeaponToken> tokens = captor.getValue();
		assertEquals(WeaponType.values().length, tokens.size());
		assertEquals(WeaponType.values().length, game.getWeaponTokens().size());
		assertEquals(WeaponType.values().length, startingBoxes.size());
		for (WeaponType weapon : WeaponType.values()) {
			WeaponToken token = tokens.stream().filter(candidate -> candidate.getWeapon() == weapon)
					.findFirst().orElseThrow();
			assertSame(game, token.getGame());
			assertSame(token.getCurrentBox(), startingBoxes.get(List.of(WeaponType.values()).indexOf(weapon)));
		}
	}

	@Test
	void shouldRejectCreationWhenGameHasNoBoard() {
		assertThrows(IllegalArgumentException.class, () -> weaponTokenService.createForGame(new Game()));
	}

	@Test
	void shouldMoveWeaponToRequestedRoomCenter() {
		Game game = new Game();
		Board board = new Board();
		game.setBoard(board);
		WeaponType weapon = WeaponType.values()[0];
		RoomType room = RoomType.KITCHEN;
		WeaponToken token = new WeaponToken();
		Box destination = new Box();
		when(weaponTokenRepository.findByGameAndWeapon(game, weapon)).thenReturn(Optional.of(token));
		when(boardService.findBox(board, BoardLayout.roomCenter(room).x(), BoardLayout.roomCenter(room).y()))
				.thenReturn(destination);
		when(weaponTokenRepository.save(token)).thenReturn(token);

		WeaponToken moved = weaponTokenService.teleportWeaponToRoom(game, weapon, room);

		assertSame(token, moved);
		assertSame(destination, token.getCurrentBox());
		verify(weaponTokenRepository).save(token);
	}

	@Test
	void shouldRejectInvalidDestinationRoomOrMissingWeapon() {
		Game game = new Game();
		game.setBoard(new Board());
		assertThrows(IllegalArgumentException.class,
				() -> weaponTokenService.teleportWeaponToRoom(game, WeaponType.values()[0], RoomType.CENTER));
		assertThrows(IllegalArgumentException.class,
				() -> weaponTokenService.teleportWeaponToRoom(game, WeaponType.values()[0], null));
		when(boardService.findBox(any(Board.class), org.mockito.ArgumentMatchers.anyInt(),
				org.mockito.ArgumentMatchers.anyInt())).thenReturn(new Box());
		when(weaponTokenRepository.findByGameAndWeapon(game, WeaponType.values()[0])).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class,
				() -> weaponTokenService.teleportWeaponToRoom(game, WeaponType.values()[0], RoomType.KITCHEN));
	}
}
