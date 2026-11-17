package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.card.CharacterCard;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCardRepository;
import es.us.dp1.L5_5_26_27.cluedo.card.RoomCard;
import es.us.dp1.L5_5_26_27.cluedo.card.WeaponCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardEffect;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRepository;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusDeckRepository;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.NotYourTurnException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.SuggestionAlreadyMadeException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameService;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnRepository;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnService;
import es.us.dp1.L5_5_26_27.cluedo.turn.TurnStatus;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponToken;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponTokenRepository;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Suggestion Module")
@Feature("Formulate and Refute Suggestions")
@Owner("DP1-team")
@SpringBootTest
@AutoConfigureTestDatabase
class SuggestionServiceTests {

	@Autowired
	private SuggestionService suggestionService;

	@Autowired
	private GameService gameService;

	@Autowired
	private TurnService turnService;

	@Autowired
	private TurnRepository turnRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private BoardService boardService;

	@Autowired
	private WeaponTokenRepository weaponTokenRepository;

	@Autowired
	private GameCardRepository gameCardRepository;

	@Autowired
	private BonusCardRepository bonusCardRepository;

	@Autowired
	private BonusDeckRepository bonusDeckRepository;

	private void asUser(String username) {
		var auth = new UsernamePasswordAuthenticationToken(
				username, "0wn3r",
				List.of(new SimpleGrantedAuthority("PLAYER")));
		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);
	}

	private Game createAndStartGameWithTurn(String roomName) {
		asUser("player1");
		Game game = gameService.createGame(roomName, 3, CharacterType.AMAPOLA);
		String code = game.getCode();

		asUser("player2");
		gameService.joinGame(code, CharacterType.RUBIO);

		asUser("player3");
		gameService.joinGame(code, CharacterType.BLANCO);

		asUser("player1");
		game = gameService.startGame(code);

		asUser("player1");
		turnService.initialRoll(code);

		asUser("player2");
		turnService.initialRoll(code);

		asUser("player3");
		turnService.initialRoll(code);

		return game;
	}

	@Test
	@Transactional
	void shouldSuggestSuccessfullyAndTeleportTokens() {
		Game game = createAndStartGameWithTurn("SalaSugerencia1");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN);
		SuggestionResponse response = suggestionService.suggest(code, request);

		assertNotNull(response);
		assertEquals(RoomType.KITCHEN, response.room());
		assertEquals(CharacterType.RUBIO, response.character());
		assertEquals(WeaponType.PISTOLA, response.weapon());
		assertEquals(SuggestionStatus.PENDING, response.status());
		assertNotNull(response.currentResponderId());

		WeaponToken gunToken = weaponTokenRepository.findByGameAndWeapon(game, WeaponType.PISTOLA).orElseThrow();
		assertNotNull(gunToken.getCurrentBox());
		assertEquals(RoomType.KITCHEN, gunToken.getCurrentBox().getRoom());

		Player suspectPlayer = playerRepository.findByGameAndCharacter(game, CharacterType.RUBIO).orElseThrow();
		assertNotNull(suspectPlayer.getCurrentBox());
		assertEquals(RoomType.KITCHEN, suspectPlayer.getCurrentBox().getRoom());
	}

	@Test
	@Transactional
	void shouldFailWhenNotActivePlayerTurn() {
		Game game = createAndStartGameWithTurn("SalaSugerencia2");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		Player otherPlayer = playerRepository.findByGameOrderByIdAsc(game).stream()
				.filter(p -> !p.getId().equals(activePlayer.getId()))
				.findFirst()
				.orElseThrow();

		boardService.teleportPlayerToRoom(otherPlayer, RoomType.LOUNGE);

		asUser(otherPlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.AMAPOLA, WeaponType.CUCHILLO, RoomType.LOUNGE);

		NotYourTurnException ex = assertThrows(NotYourTurnException.class,
				() -> suggestionService.suggest(code, request));
		assertEquals("No es tu turno", ex.getMessage());
	}

	@Test
	@Transactional
	void shouldFailWhenNotInRoom() {
		Game game = createAndStartGameWithTurn("SalaSugerencia3");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);
		var centerCoord = BoardLayout.roomCenter(RoomType.CENTER);
		activePlayer.setCurrentBox(boardService.findBox(activePlayer.getGame().getBoard(), centerCoord.x(), centerCoord.y()));
		playerRepository.save(activePlayer);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.CENTER);

		GameRuleException ex = assertThrows(GameRuleException.class, () -> suggestionService.suggest(code, request));
		assertEquals("Solo puedes formular una sugerencia desde una habitación", ex.getMessage());
	}

	@Test
	@Transactional
	void shouldFailWhenRoomDoesNotMatchCurrentRoom() {
		Game game = createAndStartGameWithTurn("SalaSugerencia4");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA,
				RoomType.DINING_ROOM);

		assertThrows(IllegalArgumentException.class, () -> suggestionService.suggest(code, request));
	}

	@Test
	@Transactional
	void shouldAskExtraQuestionWithoutMovingPlayerOrTokens() {
		Game game = createAndStartGameWithTurn("PreguntaBonusSinMovimiento");
		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();
		Player suspectPlayer = playerRepository.findByGameAndCharacter(game, CharacterType.RUBIO).orElseThrow();
		WeaponToken weaponToken = weaponTokenRepository.findByGameAndWeapon(game, WeaponType.PISTOLA).orElseThrow();
		Integer playerBoxId = activePlayer.getCurrentBox().getId();
		Integer suspectBoxId = suspectPlayer.getCurrentBox().getId();
		Integer weaponBoxId = weaponToken.getCurrentBox().getId();

		BonusCard bonusCard = new BonusCard(BonusCardEffect.EXTRA_QUESTION, true, 0);
		bonusCard.setDeck(bonusDeckRepository.findByGame(game).orElseThrow());
		bonusCard.setHolder(activePlayer);
		bonusCard = bonusCardRepository.save(bonusCard);

		asUser(activePlayer.getUser().getUsername());
		SuggestionResponse response = suggestionService.askExtraQuestion(
				game.getCode(),
				new ExtraQuestionRequest(
						bonusCard.getId(),
						CharacterType.RUBIO,
						WeaponType.PISTOLA,
						RoomType.LOUNGE));

		assertEquals(RoomType.LOUNGE, response.room());
		assertEquals(playerBoxId,
				playerRepository.findById(activePlayer.getId()).orElseThrow().getCurrentBox().getId());
		assertEquals(suspectBoxId,
				playerRepository.findById(suspectPlayer.getId()).orElseThrow().getCurrentBox().getId());
		assertEquals(weaponBoxId,
				weaponTokenRepository.findByGameAndWeapon(game, WeaponType.PISTOLA).orElseThrow().getCurrentBox()
						.getId());
		BonusCard usedCard = bonusCardRepository.findById(bonusCard.getId()).orElseThrow();
		assertTrue(usedCard.isUsed());
		assertNull(usedCard.getHolder());
	}

	@Test
	@Transactional
	void shouldFailWhenSuggestingTwiceInSameTurn() {
		Game game = createAndStartGameWithTurn("SalaSugerencia5");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN);

		SuggestionResponse first = suggestionService.suggest(code, request);
		assertNotNull(first);

		assertThrows(IllegalArgumentException.class, () -> suggestionService.suggest(code, request));
	}

	@Test
	@Transactional
	void shouldRefuteSuccessfullyAndKeepCardSecretForOthers() {
		Game game = createAndStartGameWithTurn("SalaRefutar1");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN);
		SuggestionResponse suggestResp = suggestionService.suggest(code, request);

		Player responder = playerRepository.findById(suggestResp.currentResponderId()).orElseThrow();

		List<GameCard> responderCards = gameCardRepository.findByOwner(responder);
		GameCard matchingCard = responderCards.stream()
				.filter(c -> (c instanceof CharacterCard ch && ch.getCharacter() == CharacterType.RUBIO)
						|| (c instanceof WeaponCard w && w.getWeapon() == WeaponType.PISTOLA)
						|| (c instanceof RoomCard r && r.getRoom() == RoomType.KITCHEN))
				.findFirst()
				.orElseGet(() -> {
					GameCard c = new WeaponCard(WeaponType.PISTOLA);
					c.setOwner(responder);
					return gameCardRepository.save(c);
				});

		asUser(responder.getUser().getUsername());
		SuggestionResponse refuteResp = suggestionService.refute(code, new RefuteRequest(matchingCard.getId()));

		assertNotNull(refuteResp);
		assertEquals(SuggestionStatus.REFUTED, refuteResp.status());
		assertEquals(responder.getId(), refuteResp.refutedByPlayerId());

		asUser(activePlayer.getUser().getUsername());
		SuggestionResponse visibleToSuggester = suggestionService.getCurrentSuggestion(code).orElseThrow();
		assertNotNull(visibleToSuggester.shownCard());
		assertEquals(matchingCard.getId(), visibleToSuggester.shownCard().getId());

		Player thirdPlayer = playerRepository.findByGameOrderByIdAsc(game).stream()
				.filter(p -> !p.getId().equals(activePlayer.getId()) && !p.getId().equals(responder.getId()))
				.findFirst()
				.orElseThrow();

		asUser(thirdPlayer.getUser().getUsername());
		SuggestionResponse hiddenToThirdParty = suggestionService.getCurrentSuggestion(code).orElseThrow();
		assertEquals(SuggestionStatus.REFUTED, hiddenToThirdParty.status());
		assertEquals(responder.getUser().getUsername(), hiddenToThirdParty.refutedByPlayerName());
		assertNull(hiddenToThirdParty.shownCard());
	}

	@Test
	@Transactional
	void shouldAllowThirdPlayerToPeekOnceAtTheShownCard() {
		Game game = createAndStartGameWithTurn("EchaUnVistazoBonus");
		String code = game.getCode();
		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();
		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionResponse suggested = suggestionService.suggest(
				code,
				new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN));
		Player responder = playerRepository.findById(suggested.currentResponderId()).orElseThrow();
		GameCard matchingCard = gameCardRepository.findByOwner(responder).stream()
				.filter(card -> (card instanceof CharacterCard character
						&& character.getCharacter() == CharacterType.RUBIO)
						|| (card instanceof WeaponCard weapon && weapon.getWeapon() == WeaponType.PISTOLA)
						|| (card instanceof RoomCard room && room.getRoom() == RoomType.KITCHEN))
				.findFirst()
				.orElseGet(() -> {
					GameCard card = new WeaponCard(WeaponType.PISTOLA);
					card.setOwner(responder);
					return gameCardRepository.save(card);
				});
		asUser(responder.getUser().getUsername());
		suggestionService.refute(code, new RefuteRequest(matchingCard.getId()));

		Player peekingPlayer = playerRepository.findByGameOrderByIdAsc(game).stream()
				.filter(player -> !player.getId().equals(activePlayer.getId())
						&& !player.getId().equals(responder.getId()))
				.findFirst()
				.orElseThrow();
		BonusCard peekCard = new BonusCard(BonusCardEffect.PEEK_CARD, false, 0);
		peekCard.setDeck(bonusDeckRepository.findByGame(game).orElseThrow());
		peekCard.setHolder(peekingPlayer);
		peekCard = bonusCardRepository.save(peekCard);

		asUser(peekingPlayer.getUser().getUsername());
		SuggestionResponse peeked = suggestionService.peekAtShownCard(code, peekCard.getId());

		assertEquals(matchingCard.getId(), peeked.shownCard().getId());
		assertFalse(peeked.peekAvailable());
		BonusCard usedCard = bonusCardRepository.findById(peekCard.getId()).orElseThrow();
		assertTrue(usedCard.isUsed());
		assertNull(usedCard.getHolder());
	}

	@Test
	@Transactional
	void shouldFailWhenRefutingWithNonMatchingCard() {
		Game game = createAndStartGameWithTurn("SalaRefutar2");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN);
		SuggestionResponse suggestResp = suggestionService.suggest(code, request);

		Player responder = playerRepository.findById(suggestResp.currentResponderId()).orElseThrow();

		GameCard nonMatchingCard = new WeaponCard(WeaponType.CUCHILLO);
		nonMatchingCard.setOwner(responder);
		gameCardRepository.save(nonMatchingCard);

		asUser(responder.getUser().getUsername());
		assertThrows(IllegalArgumentException.class,
				() -> suggestionService.refute(code, new RefuteRequest(nonMatchingCard.getId())));
	}

	@Test
	@Transactional
	void shouldFailWhenRefutingWithCardNotOwned() {
		Game game = createAndStartGameWithTurn("SalaRefutar3");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN);
		SuggestionResponse suggestResp = suggestionService.suggest(code, request);

		Player responder = playerRepository.findById(suggestResp.currentResponderId()).orElseThrow();

		GameCard notOwnedCard = new WeaponCard(WeaponType.PISTOLA);
		notOwnedCard.setOwner(activePlayer);
		gameCardRepository.save(notOwnedCard);

		asUser(responder.getUser().getUsername());
		assertThrows(IllegalArgumentException.class,
				() -> suggestionService.refute(code, new RefuteRequest(notOwnedCard.getId())));
	}

	@Test
	@Transactional
	void shouldPassWhenHavingNoMatchingCardsAndAdvance() {
		Game game = createAndStartGameWithTurn("SalaPasar1");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN);
		SuggestionResponse suggestResp = suggestionService.suggest(code, request);

		Player responder = playerRepository.findById(suggestResp.currentResponderId()).orElseThrow();

		for (GameCard c : gameCardRepository.findByOwner(responder)) {
			c.setOwner(null);
			gameCardRepository.save(c);
		}

		asUser(responder.getUser().getUsername());
		SuggestionResponse passResp = suggestionService.pass(code, null);

		assertNotNull(passResp);
		assertEquals(SuggestionStatus.PENDING, passResp.status());
		assertTrue(!passResp.currentResponderId().equals(responder.getId()));
	}

	@Test
	@Transactional
	void shouldFailWhenPassingWhileHavingMatchingCards() {
		Game game = createAndStartGameWithTurn("SalaPasar2");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);

		asUser(activePlayer.getUser().getUsername());
		SuggestionRequest request = new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN);
		SuggestionResponse suggestResp = suggestionService.suggest(code, request);

		Player responder = playerRepository.findById(suggestResp.currentResponderId()).orElseThrow();

		GameCard matchingCard = new WeaponCard(WeaponType.PISTOLA);
		matchingCard.setOwner(responder);
		gameCardRepository.save(matchingCard);

		asUser(responder.getUser().getUsername());
		assertThrows(IllegalArgumentException.class, () -> suggestionService.pass(code, null));
	}

	@Test
	@Transactional
	void shouldAskAnEliminatedPlayerToRefute() {
		Game game = createAndStartGameWithTurn("SalaAcusacionEliminado");
		String code = game.getCode();

		Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
				.orElseThrow();
		Player activePlayer = activeTurn.getPlayer();
		Player nextInOrder = playerRepository.findByGameOrderByIdAsc(game).stream()
				.filter(p -> p.getTurnOrder() != null && !p.getId().equals(activePlayer.getId()))
				.sorted(java.util.Comparator.comparing(Player::getTurnOrder))
				.filter(p -> p.getTurnOrder() > activePlayer.getTurnOrder())
				.findFirst()
				.orElseGet(() -> playerRepository.findByGameOrderByIdAsc(game).stream()
						.filter(p -> p.getTurnOrder() != null && !p.getId().equals(activePlayer.getId()))
						.sorted(java.util.Comparator.comparing(Player::getTurnOrder))
						.findFirst()
						.orElseThrow());
		nextInOrder.setEliminated(true);
		playerRepository.save(nextInOrder);

		boardService.teleportPlayerToRoom(activePlayer, RoomType.KITCHEN);
		asUser(activePlayer.getUser().getUsername());
		SuggestionResponse response = suggestionService.suggest(code,
				new SuggestionRequest(CharacterType.RUBIO, WeaponType.PISTOLA, RoomType.KITCHEN));

		assertEquals(nextInOrder.getId(), response.currentResponderId());
	}
}
