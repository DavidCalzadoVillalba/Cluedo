package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.card.CardService;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardEffect;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.character.CharacterTokenService;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameNotStartedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidGameCodeException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.CardNotOwnedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.NoActiveTurnException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.NotYourTurnException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.PlayerEliminatedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.PlayerNotInGameException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.SuggestionAlreadyMadeException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.SuggestionAlreadyResolvedException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameRepository;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnService;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnStatus;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponTokenService;

@Service
public class SuggestionService {

	private final GameRepository gameRepository;
	private final PlayerRepository playerRepository;
	private final TurnRepository turnRepository;
	private final SuggestionRepository suggestionRepository;
	private final BoardService boardService;
	private final WeaponTokenService weaponTokenService;
	private final CardService cardService;
	private final UserService userService;
	private final TurnService turnService;
	private final NeutralCardConsultationRepository neutralCardConsultationRepository;
	private final CharacterTokenService characterTokenService;

	public SuggestionService(
			GameRepository gameRepository,
			PlayerRepository playerRepository,
			TurnRepository turnRepository,
			SuggestionRepository suggestionRepository,
			BoardService boardService,
			WeaponTokenService weaponTokenService,
			CardService cardService,
			UserService userService,
			TurnService turnService,
			NeutralCardConsultationRepository neutralCardConsultationRepository,
			CharacterTokenService characterTokenService) {
		this.gameRepository = gameRepository;
		this.playerRepository = playerRepository;
		this.turnRepository = turnRepository;
		this.suggestionRepository = suggestionRepository;
		this.boardService = boardService;
		this.weaponTokenService = weaponTokenService;
		this.cardService = cardService;
		this.userService = userService;
		this.turnService = turnService;
		this.neutralCardConsultationRepository = neutralCardConsultationRepository;
		this.characterTokenService = characterTokenService;
	}

	@Transactional(readOnly = true)
	public List<NeutralCardResponse> getNeutralCards(String code) {
		Game game = findGame(code);
		Player player = findCurrentPlayer(game);
		Turn currentTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElse(null);
		boolean hasConsultedThisTurn = currentTurn != null
				&& neutralCardConsultationRepository.findByPlayerAndTurn(player, currentTurn).isPresent();
		List<GameCard> hiddenCards = cardService.getHiddenCards(game);
		return hiddenCards.stream().map(card -> neutralCardConsultationRepository.findByPlayerAndCard(player, card)
				.map(consultation -> new NeutralCardResponse(card.getId(), consultation.getCard(), false))
				.orElseGet(() -> new NeutralCardResponse(card.getId(), null, !hasConsultedThisTurn))).toList();
	}

	@Transactional
	public NeutralCardResponse consultNeutralCard(String code, Integer cardId) {
		Game game = findGame(code);
		Player player = findCurrentPlayer(game);
		Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(NoActiveTurnException::new);
		Suggestion suggestion = suggestionRepository.findByTurn(turn)
				.orElseThrow(() -> new GameRuleException("No hay ninguna sugerencia sin refutar"));
		if (game.getPlayers().size() != 2 || suggestion.getStatus() != SuggestionStatus.NOT_REFUTED
				|| !turn.getPlayer().getId().equals(player.getId())) {
			throw new GameRuleException("Solo quien formula una sugerencia sin refutar en una partida de dos puede consultar una carta neutral");
		}
		GameCard card = cardService.findCard(cardId)
				.orElseThrow(() -> new ResourceNotFoundException("Carta neutral", "id", cardId));
		if (card.getDeck() == null || !card.getDeck().getGame().getId().equals(game.getId())
				|| card.getOwner() != null || !card.isHidden()) {
			throw new GameRuleException("La carta seleccionada no es una carta neutral de esta partida");
		}
		Optional<NeutralCardConsultation> currentTurnConsultation = neutralCardConsultationRepository
				.findByPlayerAndTurn(player, turn);
		if (currentTurnConsultation.isPresent()) {
			NeutralCardConsultation consultation = currentTurnConsultation.get();
			if (!consultation.getCard().getId().equals(card.getId())) {
				throw new GameRuleException("Solo puedes consultar una carta neutral por turno");
			}
			return new NeutralCardResponse(card.getId(), consultation.getCard(), false);
		}
		if (neutralCardConsultationRepository.findByPlayerAndCard(player, card).isPresent()) {
			throw new GameRuleException("Ya habías consultado esta carta neutral en un turno anterior");
		}
		NeutralCardConsultation consultation = new NeutralCardConsultation();
		consultation.setPlayer(player);
		consultation.setCard(card);
		consultation.setTurn(turn);
		consultation = neutralCardConsultationRepository.save(consultation);
		return new NeutralCardResponse(card.getId(), consultation.getCard(), false);
	}

	@Transactional
	public SuggestionResponse suggest(String code, SuggestionRequest request) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}

		User currentUser = userService.findCurrentUser();
		Player player = playerRepository.findByGameAndUser(game, currentUser)
				.orElseThrow(PlayerNotInGameException::new);
		if (player.isEliminated()) {
			throw new PlayerEliminatedException("El jugador eliminado no puede formular una sugerencia");
		}
		Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(NoActiveTurnException::new);
		if (!turn.getPlayer().getId().equals(player.getId())) {
			throw new NotYourTurnException("No es tu turno");
		}
		turnService.validateAction(turn);

		Box currentBox = player.getCurrentBox();
		if (currentBox == null || currentBox.getRoom() == null || !currentBox.getRoom().isCardRoom()) {
			throw new GameRuleException("Solo puedes formular una sugerencia desde una habitación");
		}

		RoomType currentRoom = currentBox.getRoom();
		if (request.room() != null && request.room() != currentRoom) {
			throw new GameRuleException(
					"Solo puedes formular una sugerencia en la habitación en la que te encuentras");
		}

		return createSuggestion(game, currentUser, player, turn, request, currentRoom, true);
	}

	@Transactional
	public SuggestionResponse askExtraQuestion(String code, ExtraQuestionRequest request) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}

		User currentUser = userService.findCurrentUser();
		Player player = playerRepository.findByGameAndUser(game, currentUser)
				.orElseThrow(PlayerNotInGameException::new);
		if (player.isEliminated()) {
			throw new PlayerEliminatedException("El jugador eliminado no puede formular una sugerencia");
		}
		if (request.room() == RoomType.CENTER) {
			throw new GameRuleException("La pregunta extra debe referirse a una habitación");
		}

		Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(NoActiveTurnException::new);
		if (!turn.getPlayer().getId().equals(player.getId())) {
			throw new NotYourTurnException("No es tu turno");
		}
		turnService.validateAction(turn);
		if (suggestionRepository.findByTurn(turn).isPresent() || turn.getSuggestion() != null) {
			throw new SuggestionAlreadyMadeException();
		}

		BonusCard bonusCard = cardService.findBonusCardForPlayer(request.bonusCardId(), player);
		if (bonusCard.getEffect() != BonusCardEffect.EXTRA_QUESTION || !bonusCard.isImmediate()) {
			throw new GameRuleException("La carta no permite formular una pregunta extra");
		}

		SuggestionResponse response = createSuggestion(
				game,
				currentUser,
				player,
				turn,
				new SuggestionRequest(request.character(), request.weapon(), request.room()),
				request.room(),
				false);
		cardService.discardBonusCard(bonusCard);
		return response;
	}

	private SuggestionResponse createSuggestion(
			Game game,
			User currentUser,
			Player player,
			Turn turn,
			SuggestionRequest request,
			RoomType room,
			boolean movePieces) {
		if (suggestionRepository.findByTurn(turn).isPresent() || turn.getSuggestion() != null) {
			throw new SuggestionAlreadyMadeException();
		}

		if (movePieces) {
			weaponTokenService.teleportWeaponToRoom(game, request.weapon(), room);
			characterTokenService.teleportCharacterToRoom(game, request.character(), room);
			playerRepository.findByGameAndCharacter(game, request.character())
					.ifPresent(suspectPlayer -> boardService.teleportPlayerToRoom(suspectPlayer, room));
		}

		List<Player> responders = playersWhoCanRefute(game);

		int currentIndex = responders.indexOf(player);
		Player firstResponder = null;
		SuggestionStatus status = SuggestionStatus.NOT_REFUTED;
		if (responders.size() > 1 && currentIndex >= 0) {
			firstResponder = responders.get((currentIndex + 1) % responders.size());
			status = SuggestionStatus.PENDING;
			turn.setExpiresAt(Instant.now().plusSeconds(25));
		} else {
			turn.setExpiresAt(Instant.now().plusSeconds(35));
		}

		Suggestion suggestion = new Suggestion();
		suggestion.setTurn(turn);
		suggestion.setRoom(room);
		suggestion.setCharacter(request.character());
		suggestion.setWeapon(request.weapon());
		suggestion.setStatus(status);
		suggestion.setFinalAccusation(false);
		suggestion.setCurrentResponder(firstResponder);

		Suggestion saved = suggestionRepository.save(suggestion);
		turn.setSuggestion(saved);
		turnRepository.save(turn);

		return SuggestionResponse.from(saved, currentUser.getId());
	}

	@Transactional
	public SuggestionResponse peekAtShownCard(String code, Integer bonusCardId) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}

		User currentUser = userService.findCurrentUser();
		Player player = playerRepository.findByGameAndUser(game, currentUser)
				.orElseThrow(PlayerNotInGameException::new);
		if (player.isEliminated()) {
			throw new PlayerEliminatedException("El jugador eliminado no puede usar una carta bonus");
		}
		Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(NoActiveTurnException::new);
		Suggestion suggestion = suggestionRepository.findByTurn(turn)
				.orElseThrow(() -> new GameRuleException("No hay ninguna sugerencia resuelta"));
		if (suggestion.getStatus() != SuggestionStatus.REFUTED || suggestion.getShownCard() == null) {
			throw new GameRuleException("Solo puedes echar un vistazo después de una refutación");
		}
		if (suggestion.getPeekedBy() != null) {
			throw new GameRuleException(
					"Otro jugador ya ha usado Echa un vistazo en esta sugerencia");
		}
		if (player.getId().equals(suggestion.getTurn().getPlayer().getId())
				|| player.getId().equals(suggestion.getCurrentResponder().getId())) {
			throw new GameRuleException("Ya has visto la carta mostrada");
		}

		BonusCard bonusCard = cardService.findBonusCardForPlayer(bonusCardId, player);
		if (bonusCard.getEffect() != BonusCardEffect.PEEK_CARD) {
			throw new GameRuleException("La carta no permite echar un vistazo");
		}

		suggestion.setPeekedBy(player);
		suggestionRepository.save(suggestion);
		cardService.discardBonusCard(bonusCard);
		return SuggestionResponse.from(suggestion, currentUser.getId());
	}

	@Transactional
	public SuggestionResponse refute(String code, RefuteRequest request) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}

		User currentUser = userService.findCurrentUser();
		Player player = playerRepository.findByGameAndUser(game, currentUser)
				.orElseThrow(PlayerNotInGameException::new);

		Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(NoActiveTurnException::new);

		Suggestion suggestion = suggestionRepository.findByTurn(turn)
				.orElseThrow(() -> new GameRuleException("No hay ninguna hipótesis pendiente en este turno"));

		if (suggestion.getStatus() != SuggestionStatus.PENDING) {
			throw new SuggestionAlreadyResolvedException();
		}

		if (suggestion.getCurrentResponder() == null
				|| !suggestion.getCurrentResponder().getId().equals(player.getId())) {
			throw new NotYourTurnException("No es tu turno de refutar");
		}

		GameCard card = cardService.findCard(request.cardId())
				.orElseThrow(() -> new ResourceNotFoundException("Carta", "id", request.cardId()));

		if (card.getOwner() == null || !card.getOwner().getId().equals(player.getId())) {
			throw new CardNotOwnedException();
		}

		if (!cardService.matchesSuggestion(
				card, suggestion.getCharacter(), suggestion.getWeapon(), suggestion.getRoom())) {
			throw new GameRuleException("La carta seleccionada no coincide con ningún elemento de la sugerencia");
		}

		suggestion.setStatus(SuggestionStatus.REFUTED);
		suggestion.setShownCard(card);
		suggestion.setCurrentResponder(player);
		turn.setExpiresAt(Instant.now().plusSeconds(35));

		suggestionRepository.save(suggestion);
		turnRepository.save(turn);

		return SuggestionResponse.from(suggestion, currentUser.getId());
	}

	@Transactional
	public SuggestionResponse pass(String code, Integer bonusCardId) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}

		User currentUser = userService.findCurrentUser();
		Player player = playerRepository.findByGameAndUser(game, currentUser)
				.orElseThrow(PlayerNotInGameException::new);

		Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow(NoActiveTurnException::new);

		Suggestion suggestion = suggestionRepository.findByTurn(turn)
				.orElseThrow(() -> new GameRuleException("No hay ninguna hipótesis pendiente en este turno"));

		if (suggestion.getStatus() != SuggestionStatus.PENDING) {
			throw new SuggestionAlreadyResolvedException();
		}

		if (suggestion.getCurrentResponder() == null
				|| !suggestion.getCurrentResponder().getId().equals(player.getId())) {
			throw new NotYourTurnException("No es tu turno de responder");
		}

		if (bonusCardId != null) {
			BonusCard bonusCard = cardService.findBonusCardForPlayer(bonusCardId, player);
			if (bonusCard.getEffect() != BonusCardEffect.SKIP_REFUTATION) {
				throw new GameRuleException("La carta bonus no es de tipo SKIP_REFUTATION");
			}
			cardService.discardBonusCard(bonusCard);
		} else {
			List<GameCard> matching = cardService.getMatchingCardsForPlayer(
					player, suggestion.getCharacter(), suggestion.getWeapon(), suggestion.getRoom());
			if (!matching.isEmpty()) {
				throw new GameRuleException(
						"Tienes al menos una carta que coincide y estás obligado a mostrarla");
			}
		}

		List<Player> responders = playersWhoCanRefute(game);

		int currentIndex = responders.indexOf(player);
		Player nextResponder = responders.get((currentIndex + 1) % responders.size());

		if (nextResponder.getId().equals(turn.getPlayer().getId())) {
			suggestion.setStatus(SuggestionStatus.NOT_REFUTED);
			suggestion.setCurrentResponder(null);
			turn.setExpiresAt(Instant.now().plusSeconds(35));
		} else {
			suggestion.setCurrentResponder(nextResponder);
			turn.setExpiresAt(Instant.now().plusSeconds(25));
		}

		suggestionRepository.save(suggestion);
		turnRepository.save(turn);

		return SuggestionResponse.from(suggestion, currentUser.getId());
	}

	@Transactional(readOnly = true)
	public Optional<SuggestionResponse> getCurrentSuggestion(String code) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));

		User currentUser = userService.findCurrentUser();

		return turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.flatMap(suggestionRepository::findByTurn)
				.map(suggestion -> SuggestionResponse.from(suggestion, currentUser.getId()));
	}

	private List<Player> playersWhoCanRefute(Game game) {
		// RN-9: el eliminado no se mueve ni sugiere, pero sus cartas siguen refutando.
		return playerRepository.findByGameOrderByIdAsc(game).stream()
				.filter(p -> p.getTurnOrder() != null)
				.sorted(Comparator.comparing(Player::getTurnOrder))
				.toList();
	}

	private String normalizeCode(String code) {
		if (code == null || code.isBlank()) {
			throw new InvalidGameCodeException();
		}
		String normalized = code.trim().toUpperCase();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		return normalized;
	}

	private Game findGame(String code) {
		String normalizedCode = normalizeCode(code);
		Game game = gameRepository.findByCode(normalizedCode)
				.orElseThrow(() -> new ResourceNotFoundException("Partida", "código", normalizedCode));
		if (game.getStatus() != GameStatus.STARTED) {
			throw new GameNotStartedException("La partida no está en curso");
		}
		return game;
	}

	private Player findCurrentPlayer(Game game) {
		User currentUser = userService.findCurrentUser();
		return playerRepository.findByGameAndUser(game, currentUser).orElseThrow(PlayerNotInGameException::new);
	}
}
