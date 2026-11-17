package es.us.dp1.L5_5_26_27.cluedo.character;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.Board;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout.Coord;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.BoardNotInitializedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;

@Service
public class CharacterTokenService {

	private final CharacterTokenRepository characterTokenRepository;
	private final BoardService boardService;

	public CharacterTokenService(CharacterTokenRepository characterTokenRepository, BoardService boardService) {
		this.characterTokenRepository = characterTokenRepository;
		this.boardService = boardService;
	}

	@Transactional
	public List<CharacterToken> createForGame(Game game) {
		Board board = game.getBoard();
		if (board == null) {
			throw new BoardNotInitializedException("La partida no tiene tablero para colocar las fichas de personaje");
		}

		List<CharacterToken> existingTokens = characterTokenRepository.findByGame(game);
		if (!existingTokens.isEmpty()) {
			return existingTokens;
		}

		List<CharacterToken> tokens = new ArrayList<>();
		for (CharacterType character : CharacterType.values()) {
			CharacterToken token = new CharacterToken();
			token.setGame(game);
			token.setCharacter(character);

			Optional<Player> assignedPlayer = game.getPlayers() != null
					? game.getPlayers().stream().filter(p -> p.getCharacter() == character).findFirst()
					: Optional.empty();

			if (assignedPlayer.isPresent() && assignedPlayer.get().getCurrentBox() != null) {
				token.setCurrentBox(assignedPlayer.get().getCurrentBox());
			} else {
				Coord start = BoardLayout.CHARACTER_STARTS.get(character);
				if (start != null) {
					token.setCurrentBox(boardService.findBox(board, start.x(), start.y()));
				}
			}
			tokens.add(token);
		}
		characterTokenRepository.saveAll(tokens);
		game.getCharacterTokens().clear();
		game.getCharacterTokens().addAll(tokens);
		return tokens;
	}

	@Transactional(readOnly = true)
	public List<CharacterToken> findByGame(Game game) {
		return characterTokenRepository.findByGameOrderByCharacterAsc(game);
	}

	@Transactional
	public CharacterToken teleportCharacterToRoom(Game game, CharacterType character, RoomType room) {
		if (room == null || !room.isCardRoom()) {
			throw new GameRuleException("Debes elegir una habitación válida para mover el personaje");
		}
		Board board = game.getBoard();
		if (board == null) {
			throw new BoardNotInitializedException();
		}
		Coord center = BoardLayout.roomCenter(room);
		Box destination = boardService.findBox(board, center.x(), center.y());
		CharacterToken token = characterTokenRepository.findByGameAndCharacter(game, character)
				.orElseGet(() -> {
					CharacterToken newToken = new CharacterToken();
					newToken.setGame(game);
					newToken.setCharacter(character);
					return newToken;
				});
		token.setCurrentBox(destination);
		return characterTokenRepository.save(token);
	}

	@Transactional
	public void updateCharacterBox(Game game, CharacterType character, Box box) {
		if (game == null || character == null || box == null) {
			return;
		}
		characterTokenRepository.findByGameAndCharacter(game, character).ifPresent(token -> {
			token.setCurrentBox(box);
			characterTokenRepository.save(token);
		});
	}

}
