package es.us.dp1.L5_5_26_27.cluedo.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout.Coord;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout.SecretPassage;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.BoxType;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;

@Component
public class BoardInitializer {

	private final PlayerRepository playerRepository;

	public BoardInitializer(PlayerRepository playerRepository) {
		this.playerRepository = playerRepository;
	}

	public Box[][] initializeBoard(Board board) {
		Box[][] grid = buildGrid(board);
		linkSecretPassages(grid);

		List<Box> boxes = new ArrayList<>(BoardLayout.WIDTH * BoardLayout.HEIGHT);
		for (int y = 0; y < BoardLayout.HEIGHT; y++) {
			for (int x = 0; x < BoardLayout.WIDTH; x++) {
				boxes.add(grid[y][x]);
			}
		}
		board.setBoxes(boxes);
		return grid;
	}

	public Box[][] buildGrid(Board board) {
		Set<String> doorKeys = toKeySet(BoardLayout.DOORS);
		Set<String> bonusKeys = toKeySet(BoardLayout.BONUS_CELLS);
		Map<String, SecretPassage> passagesByOrigin = new HashMap<>();
		for (SecretPassage passage : BoardLayout.SECRET_PASSAGES) {
			passagesByOrigin.put(key(passage.from()), passage);
		}

		Box[][] grid = new Box[BoardLayout.HEIGHT][BoardLayout.WIDTH];
		for (int y = 0; y < BoardLayout.HEIGHT; y++) {
			for (int x = 0; x < BoardLayout.WIDTH; x++) {
				Box box = new Box();
				box.setBoard(board);
				box.setX(x);
				box.setY(y);

				String cellKey = key(x, y);
				SecretPassage passage = passagesByOrigin.get(cellKey);
				RoomType room = BoardLayout.findRoomAt(x, y);

				if (passage != null) {
					box.setType(BoxType.SECRET_PASSAGE);
					box.setRoom(passage.room());
				} else if (bonusKeys.contains(cellKey)) {
					box.setType(BoxType.BONUS);
				} else if (room != null && doorKeys.contains(cellKey)) {
					box.setType(BoxType.DOOR);
					box.setRoom(room);
				} else if (room != null) {
					box.setType(BoxType.ROOM);
					box.setRoom(room);
				} else {
					box.setType(BoxType.PATH);
				}

				grid[y][x] = box;
			}
		}
		return grid;
	}

	public void linkSecretPassages(Box[][] grid) {
		for (SecretPassage passage : BoardLayout.SECRET_PASSAGES) {
			Box from = grid[passage.from().y()][passage.from().x()];
			Box to = grid[passage.to().y()][passage.to().x()];
			from.setSecretPassageDestination(to);
		}
	}

	public void placePlayers(Game game, Box[][] grid) {
		if (game.getPlayers() == null || game.getPlayers().isEmpty()) {
			return;
		}
		for (Player player : game.getPlayers()) {
			Coord start = BoardLayout.CHARACTER_STARTS.get(player.getCharacter());
			if (start == null) {
				throw new GameRuleException(
						"No hay casilla de salida para el personaje " + player.getCharacter());
			}
			player.setCurrentBox(grid[start.y()][start.x()]);
		}
		if (playerRepository != null) {
			playerRepository.saveAll(game.getPlayers());
		}
	}

	private static Set<String> toKeySet(List<Coord> coords) {
		Set<String> keys = new HashSet<>();
		for (Coord coord : coords) {
			keys.add(key(coord));
		}
		return keys;
	}

	private static String key(Coord coord) {
		return key(coord.x(), coord.y());
	}

	private static String key(int x, int y) {
		return x + "," + y;
	}
}
