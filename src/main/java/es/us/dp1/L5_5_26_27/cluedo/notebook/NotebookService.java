package es.us.dp1.L5_5_26_27.cluedo.notebook;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

@Service
public class NotebookService {

	private final NotebookMarkRepository notebookMarkRepository;
	private final GameRepository gameRepository;
	private final PlayerRepository playerRepository;
	private final UserService userService;

	public NotebookService(
			NotebookMarkRepository notebookMarkRepository,
			GameRepository gameRepository,
			PlayerRepository playerRepository,
			UserService userService) {
		this.notebookMarkRepository = notebookMarkRepository;
		this.gameRepository = gameRepository;
		this.playerRepository = playerRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<NotebookMarkResponse> getMarks(String gameCode) {
		Player player = findCurrentPlayer(gameCode);
		return notebookMarkRepository.findByPlayerOrderByCategoryAscIdAsc(player).stream()
				.map(NotebookMarkResponse::from)
				.toList();
	}

	@Transactional
	public NotebookMarkResponse updateMark(String gameCode, Integer markId, NotebookMarkValue value) {
		Player player = findCurrentPlayer(gameCode);
		NotebookMark mark = notebookMarkRepository.findByIdAndPlayer(markId, player)
				.orElseThrow(() -> new ResourceNotFoundException("NotebookMark", "id", markId));
		mark.setValue(value);
		return NotebookMarkResponse.from(notebookMarkRepository.save(mark));
	}

	@Transactional
	public void initializeForGame(Game game) {
		List<NotebookMark> marks = new ArrayList<>();
		for (Player player : game.getPlayers()) {
			addCharacterMarks(marks, player);
			addWeaponMarks(marks, player);
			addRoomMarks(marks, player);
		}
		notebookMarkRepository.saveAll(marks);
	}

	private Player findCurrentPlayer(String gameCode) {
		Game game = gameRepository.findByCode(gameCode)
				.orElseThrow(() -> new ResourceNotFoundException("Game", "code", gameCode));
		User currentUser = userService.findCurrentUser();
		return playerRepository.findByGameAndUser(game, currentUser)
				.orElseThrow(() -> new ResourceNotFoundException("Player", "game code", gameCode));
	}

	private void addCharacterMarks(List<NotebookMark> marks, Player player) {
		for (CharacterType character : CharacterType.values()) {
			NotebookMark mark = newMark(player, NotebookCategory.SUSPECT);
			mark.setCharacter(character);
			marks.add(mark);
		}
	}

	private void addWeaponMarks(List<NotebookMark> marks, Player player) {
		for (WeaponType weapon : WeaponType.values()) {
			NotebookMark mark = newMark(player, NotebookCategory.WEAPON);
			mark.setWeapon(weapon);
			marks.add(mark);
		}
	}

	private void addRoomMarks(List<NotebookMark> marks, Player player) {
		for (RoomType room : RoomType.values()) {
			if (!room.isCardRoom()) {
				continue;
			}
			NotebookMark mark = newMark(player, NotebookCategory.ROOM);
			mark.setRoom(room);
			marks.add(mark);
		}
	}

	private NotebookMark newMark(Player player, NotebookCategory category) {
		NotebookMark mark = new NotebookMark();
		mark.setPlayer(player);
		mark.setCategory(category);
		mark.setValue(NotebookMarkValue.BLANK);
		return mark;
	}
}
