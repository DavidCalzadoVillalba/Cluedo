package es.us.dp1.L5_5_26_27.cluedo.weapon;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.Board;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout.Coord;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.BoardNotInitializedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;

@Service
public class WeaponTokenService {

	private final WeaponTokenRepository weaponTokenRepository;
	private final BoardService boardService;

	public WeaponTokenService(WeaponTokenRepository weaponTokenRepository, BoardService boardService) {
		this.weaponTokenRepository = weaponTokenRepository;
		this.boardService = boardService;
	}

	@Transactional
	public void createForGame(Game game) {
		Board board = game.getBoard();
		if (board == null) {
			throw new BoardNotInitializedException("La partida no tiene tablero para colocar las armas");
		}

		List<WeaponToken> tokens = new ArrayList<>();
		for (WeaponType weapon : WeaponType.values()) {
			WeaponToken token = new WeaponToken();
			token.setGame(game);
			token.setWeapon(weapon);
			token.setCurrentBox(startingBox(board, weapon));
			tokens.add(token);
		}
		weaponTokenRepository.saveAll(tokens);
		game.getWeaponTokens().addAll(tokens);
	}

	@Transactional
	public WeaponToken teleportWeaponToRoom(Game game, WeaponType weapon, RoomType room) {
		if (room == null || !room.isCardRoom()) {
			throw new GameRuleException("Debes elegir una habitación válida para mover el arma");
		}
		Board board = game.getBoard();
		if (board == null) {
			throw new BoardNotInitializedException();
		}
		Coord center = BoardLayout.roomCenter(room);
		Box destination = boardService.findBox(board, center.x(), center.y());
		WeaponToken token = weaponTokenRepository.findByGameAndWeapon(game, weapon)
				.orElseThrow(() -> new ResourceNotFoundException("Arma", "partida", game.getCode()));
		token.setCurrentBox(destination);
		return weaponTokenRepository.save(token);
	}

	private Box startingBox(Board board, WeaponType weapon) {
		RoomType room = BoardLayout.WEAPON_START_ROOMS.get(weapon);
		Coord center = BoardLayout.roomCenter(room);
		return boardService.findBox(board, center.x(), center.y());
	}
}
