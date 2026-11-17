package es.us.dp1.L5_5_26_27.cluedo.game;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.card.CardService;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRevealResponse;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.CharacterAlreadySelectedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.CharacterSelectionRequiredException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.DuplicateGameNameException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.GameFullException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.GameNotWaitingException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.HostCannotKickSelfException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InsufficientPlayersException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidGameCodeException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidGameNameException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidPlayerCountException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.PlayerAlreadyInGameException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.character.CharacterToken;
import es.us.dp1.L5_5_26_27.cluedo.character.CharacterTokenService;
import es.us.dp1.L5_5_26_27.cluedo.notebook.NotebookService;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.suggestion.Suggestion;
import es.us.dp1.L5_5_26_27.cluedo.suggestion.SuggestionResponse;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnStatus;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponToken;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponTokenRepository;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponTokenService;

@Service
public class GameService {

	private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int CODE_LENGTH = 6;

	private final GameRepository gameRepository;
	private final PlayerRepository playerRepository;
	private final UserService userService;
	private final CardService cardService;
	private final NotebookService notebookService;
	private final WeaponTokenService weaponTokenService;
	private final WeaponTokenRepository weaponTokenRepository;
	private final BoardService boardService;
	private final TurnRepository turnRepository;
	private final CharacterTokenService characterTokenService;
	private final Random random = new Random();

	@Autowired
	public GameService(
			GameRepository gameRepository,
			PlayerRepository playerRepository,
			UserService userService,
			CardService cardService,
			NotebookService notebookService,
			WeaponTokenService weaponTokenService,
			WeaponTokenRepository weaponTokenRepository,
			BoardService boardService,
			TurnRepository turnRepository,
			CharacterTokenService characterTokenService) {
		this.gameRepository = gameRepository;
		this.playerRepository = playerRepository;
		this.userService = userService;
		this.cardService = cardService;
		this.notebookService = notebookService;
		this.weaponTokenService = weaponTokenService;
		this.weaponTokenRepository = weaponTokenRepository;
		this.boardService = boardService;
		this.turnRepository = turnRepository;
		this.characterTokenService = characterTokenService;
	}

	@Transactional
	public Game createGame(String name, Integer maxPlayers, CharacterType character) {
		String roomName = name == null ? "" : name.trim();
		if (roomName.length() < 3 || roomName.length() > 40) {
			throw new InvalidGameNameException();
		}
		if (maxPlayers == null || maxPlayers < 2 || maxPlayers > 6) {
			throw new InvalidPlayerCountException();
		}
		if (character == null) {
			throw new CharacterSelectionRequiredException();
		}
		if (gameRepository.existsByNameIgnoreCaseAndStatus(roomName, GameStatus.WAITING)) {
			throw new DuplicateGameNameException();
		}

		User host = userService.findCurrentUser();

		Game game = new Game();
		game.setName(roomName);
		game.setMaxPlayers(maxPlayers);
		game.setStatus(GameStatus.WAITING);
		game.setCode(generateCode());
		game.setHost(host);
		gameRepository.save(game);

		Player hostPlayer = new Player();
		hostPlayer.setUser(host);
		hostPlayer.setGame(game);
		hostPlayer.setCharacter(character);
		playerRepository.save(hostPlayer);
		game.getPlayers().add(hostPlayer);

		return game;
	}

	@Transactional(readOnly = true)
	public List<ActiveGameResponse> getAllGamesForAdministration() {
		User current = userService.findCurrentUser();
		if (!current.hasAuthority("ADMIN")) {
			throw new AccessDeniedException("Necesitas el Rol de Administrador para consultar la lista de partidas");
		}
		return gameRepository.findAll().stream().map(ActiveGameResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public List<GameHistoryResponse> getCurrentUserHistory() {
		User current = userService.findCurrentUser();
		return gameRepository.findHistoryByUserAndStatus(current, GameStatus.ENDED).stream()
				.map(game -> GameHistoryResponse.from(game, current)).toList();
	}

	@Transactional(readOnly = true)
	public Game validateAccess(String code) {
		Game game = findWaitingGame(code);
		User current = userService.findCurrentUser();

		if (playerRepository.findByGameAndUser(game, current).isPresent()) {
			throw new PlayerAlreadyInGameException();
		}
		if (game.getPlayers().size() >= game.getMaxPlayers()) {
			throw new GameFullException();
		}
		return game;
	}

	@Transactional
	public Game joinGame(String code, CharacterType character) {
		if (character == null) {
			throw new CharacterSelectionRequiredException();
		}

		Game game = findWaitingGame(code);
		User current = userService.findCurrentUser();

		if (playerRepository.findByGameAndUser(game, current).isPresent()) {
			throw new PlayerAlreadyInGameException();
		}
		if (game.getPlayers().size() >= game.getMaxPlayers()) {
			throw new GameFullException();
		}
		// Última comprobación: otro jugador puede haber cogido el personaje mientras se
		// elegía.
		if (playerRepository.existsByGameAndCharacter(game, character)) {
			throw new CharacterAlreadySelectedException();
		}

		Player player = new Player();
		player.setUser(current);
		player.setGame(game);
		player.setCharacter(character);
		playerRepository.save(player);
		game.getPlayers().add(player);

		return game;
	}

	@Transactional(readOnly = true)
	public Game getLobby(String code) {
		Game game = findWaitingGame(code);
		User current = userService.findCurrentUser();

		if (playerRepository.findByGameAndUser(game, current).isEmpty()) {
			throw new AccessDeniedException("No perteneces a esta partida");
		}
		return game;
	}

	@Transactional(readOnly = true)
	public GameStateResponse getGameState(String code) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		User current = userService.findCurrentUser();

		Player currentPlayer = playerRepository.findByGameAndUser(game, current)
				.orElseThrow(() -> new AccessDeniedException("No perteneces a esta partida"));

		Turn activeTurn = game.getStatus() == GameStatus.STARTED
				? turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
						.orElse(null)
				: null;

		List<WeaponToken> weaponTokens = game.getStatus() == GameStatus.STARTED
				? weaponTokenRepository.findByGameOrderByWeaponAsc(game)
				: List.of();

		List<GameCard> myHand = game.getStatus() == GameStatus.STARTED
				? cardService.getHandForPlayer(currentPlayer)
				: List.of();

		List<BonusCard> myBonusCards = game.getStatus() == GameStatus.STARTED
				? cardService.getBonusHandForPlayer(currentPlayer)
				: List.of();

		List<GameCard> faceUpCards = game.getStatus() == GameStatus.STARTED
				? cardService.getFaceUpCards(game)
				: List.of();

		Suggestion activeSuggestion = activeTurn != null ? activeTurn.getSuggestion() : null;
		SuggestionResponse activeSuggestionResponse = activeSuggestion != null
				? SuggestionResponse.from(activeSuggestion, current.getId())
				: null;

		List<BonusCardRevealResponse> pendingBonusReveals = game.getStatus() == GameStatus.STARTED
				? cardService.getPendingPublicReveals(game, currentPlayer)
				: List.of();
		List<BonusCardRevealResponse> publicBonusReveals = game.getStatus() == GameStatus.STARTED
				? cardService.getPublicReveals(game)
				: List.of();

		List<CharacterToken> characterTokens = game.getStatus() == GameStatus.STARTED || game.getStatus() == GameStatus.ENDED
				? characterTokenService.findByGame(game)
				: List.of();
		if (game.getStatus() == GameStatus.STARTED && characterTokens.isEmpty() && game.getBoard() != null) {
			characterTokens = characterTokenService.createForGame(game);
		}

		return GameStateResponse.from(game, activeTurn, weaponTokens, characterTokens, myHand, myBonusCards, faceUpCards,
				activeSuggestionResponse, pendingBonusReveals, publicBonusReveals);

	}

	@Transactional
	public Game kickPlayer(String code, Integer playerId) {
		Game game = findWaitingGame(code);
		User current = userService.findCurrentUser();

		if (!game.getHost().getId().equals(current.getId())) {
			throw new AccessDeniedException("Solo el anfitrión puede expulsar jugadores");
		}

		Player player = playerRepository.findById(playerId)
				.orElseThrow(() -> new ResourceNotFoundException("Jugador", "id", playerId));
		if (player.getGame() == null || !game.getId().equals(player.getGame().getId())) {
			throw new ResourceNotFoundException("Jugador", "id", playerId);
		}
		if (player.getUser().getId().equals(current.getId())) {
			throw new HostCannotKickSelfException();
		}

		game.getPlayers().remove(player);
		playerRepository.delete(player);
		return game;
	}

	@Transactional
	public Game startGame(String code) {
		Game game = findWaitingGame(code);
		User current = userService.findCurrentUser();

		if (!game.getHost().getId().equals(current.getId())) {
			throw new AccessDeniedException("Solo el anfitrión puede iniciar la partida");
		}
		if (game.getPlayers().size() < 2) {
			throw new InsufficientPlayersException();
		}

		List<Player> initialOrder = new ArrayList<>(game.getPlayers());
		Collections.shuffle(initialOrder, random);
		for (int i = 0; i < initialOrder.size(); i++) {
			Player player = initialOrder.get(i);
			player.setTurnOrder(i + 1);
			player.setInitialDiceResult(null);
			player.setInitialDiceOne(null);
			player.setInitialDiceTwo(null);
		}
		playerRepository.saveAll(initialOrder);
		game.setStatus(GameStatus.STARTED);
		boardService.createForGame(game);
		cardService.createAndDealCards(game);
		cardService.createBonusDeck(game);
		notebookService.initializeForGame(game);
		weaponTokenService.createForGame(game);
		characterTokenService.createForGame(game);

		return gameRepository.save(game);
	}

	private Game findWaitingGame(String code) {
		String normalized = normalizeCode(code);
		Game game = gameRepository.findByCode(normalized)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalized));
		if (game.getStatus() != GameStatus.WAITING) {
			throw new GameNotWaitingException();
		}
		return game;
	}

	private String normalizeCode(String code) {
		if (code == null || code.isBlank()) {
			throw new InvalidGameCodeException();
		}
		String normalized = code.trim().toUpperCase();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		if (normalized.isBlank()) {
			throw new InvalidGameCodeException();
		}
		return normalized;
	}

	private String generateCode() {
		String code;
		do {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < CODE_LENGTH; i++) {
				builder.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
			}
			code = builder.toString();
		} while (gameRepository.existsByCode(code));
		return code;
	}

}
