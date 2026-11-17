package es.us.dp1.L5_5_26_27.cluedo.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardEffect;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRepository;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRevealRepository;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusDeck;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusDeckRepository;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardService;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

@ExtendWith(MockitoExtension.class)
class CardServiceTests {

	@Mock
	private GameCardRepository gameCardRepository;

	@Mock
	private GameDeckRepository gameDeckRepository;

	@Mock
	private BonusCardRepository bonusCardRepository;

	@Mock
	private BonusDeckRepository bonusDeckRepository;

	@Mock
	private BonusCardRevealRepository bonusCardRevealRepository;

	@Mock
	private SolutionRepository solutionRepository;

	@Mock
	private PlayerRepository playerRepository;

	private CardService cardService;

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game();
		game.setId(1);
		lenient().when(gameDeckRepository.save(any(GameDeck.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		lenient().when(bonusDeckRepository.save(any(BonusDeck.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		GameCardDealer dealer = new GameCardDealer(gameDeckRepository, solutionRepository,
				gameCardRepository, playerRepository);
		BonusCardService bonusSvc = new BonusCardService(bonusCardRepository, bonusDeckRepository,
				bonusCardRevealRepository, gameCardRepository);
		cardService = new CardService(gameCardRepository, gameDeckRepository, dealer, bonusSvc);
	}

	private List<Player> players(int count) {
		List<Player> players = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Player player = new Player();
			player.setId(i + 1);
			player.setGame(game);
			players.add(player);
		}
		game.setPlayers(players);
		return players;
	}

	private List<GameCard> dealtCards() {
		ArgumentCaptor<List<GameCard>> captor = ArgumentCaptor.forClass(List.class);
		verify(gameCardRepository).saveAll(captor.capture());
		return captor.getValue();
	}

	private List<List<GameCard>> allDealtCards() {
		ArgumentCaptor<List<GameCard>> captor = ArgumentCaptor.forClass(List.class);
		verify(gameCardRepository, org.mockito.Mockito.times(4)).saveAll(captor.capture());
		return captor.getAllValues();
	}

	@Test
	@DisplayName("La solución contiene exactamente un personaje, un arma y una habitación")
	void shouldCreateSolutionWithOneCardOfEachType() {
		players(3);

		cardService.createAndDealCards(game);

		ArgumentCaptor<Solution> solutionCaptor = ArgumentCaptor.forClass(Solution.class);
		verify(solutionRepository).save(solutionCaptor.capture());
		Solution solution = solutionCaptor.getValue();
		assertNotNull(solution.getCharacterSolution());
		assertNotNull(solution.getWeaponSolution());
		assertNotNull(solution.getRoomSolution());
		assertEquals(game, solution.getGame());
		assertEquals(1, CharacterType.values().length - countCharacterCards(dealtCards()));
	}

	private long countCharacterCards(List<GameCard> cards) {
		return cards.stream().filter(CharacterCard.class::isInstance).count();
	}

	@Test
	@DisplayName("Con dos jugadores aparta cuatro cartas neutrales boca abajo y reparte siete a cada uno")
	void shouldDealTwoPlayerGameWithFourHiddenNeutralCards() {
		List<Player> players = players(2);

		cardService.createAndDealCards(game);

		List<GameCard> cards = dealtCards();
		assertEquals(18, cards.size());
		assertEquals(4, cards.stream().filter(GameCard::isHidden).count());
		assertTrue(cards.stream().filter(GameCard::isHidden).allMatch(card -> card.getOwner() == null));
		assertEquals(14, cards.stream().filter(card -> card.getOwner() != null).count());
		assertEquals(7, players.get(0).getHand().size());
		assertEquals(7, players.get(1).getHand().size());
		assertTrue(cards.stream().filter(card -> card.getOwner() != null).noneMatch(GameCard::isHidden));
	}

	@Test
	@DisplayName("Entre tres y seis jugadores reparte equitativamente y deja visibles las sobrantes")
	void shouldDealThreeToSixPlayerGamesEquallyAndLeaveRemaindersFaceUp() {
		List<List<Player>> playerGroups = new ArrayList<>();
		for (int playerCount = 3; playerCount <= 6; playerCount++) {
			game = new Game();
			game.setId(playerCount);
			List<Player> players = players(playerCount);
			playerGroups.add(players);

			cardService.createAndDealCards(game);
		}
		List<List<GameCard>> cardGroups = allDealtCards();
		for (int playerCount = 3; playerCount <= 6; playerCount++) {
			List<Player> players = playerGroups.get(playerCount - 3);
			List<GameCard> cards = cardGroups.get(playerCount - 3);
			int cardsPerPlayer = 18 / playerCount;
			int remainder = 18 % playerCount;
			assertEquals(18, cards.size());
			assertTrue(players.stream().allMatch(player -> player.getHand().size() == cardsPerPlayer));
			assertEquals(remainder, cards.stream().filter(card -> card.getOwner() == null).count());
			assertTrue(cards.stream().filter(card -> card.getOwner() == null).noneMatch(GameCard::isHidden));
			assertTrue(cards.stream().noneMatch(GameCard::isHidden));
		}
	}

	@Test
	@DisplayName("Encuentra solo las cartas que coinciden con la sugerencia en la mano del jugador")
	void shouldReturnMatchingCardsForPlayer() {
		Player player = new Player();
		CharacterType character = CharacterType.values()[0];
		WeaponType weapon = WeaponType.values()[0];
		RoomType room = RoomType.values()[0];
		CharacterCard matchingCharacter = new CharacterCard(character);
		WeaponCard matchingWeapon = new WeaponCard(weapon);
		RoomCard matchingRoom = new RoomCard(room);
		CharacterCard otherCharacter = new CharacterCard(CharacterType.values()[1]);
		player.setHand(List.of(matchingCharacter, matchingWeapon, matchingRoom, otherCharacter));
		when(gameCardRepository.findByOwner(player)).thenReturn(player.getHand());

		List<GameCard> matches = cardService.getMatchingCardsForPlayer(player, character, weapon, room);

		assertEquals(3, matches.size());
		assertTrue(matches.containsAll(List.of(matchingCharacter, matchingWeapon, matchingRoom)));
		assertFalse(matches.stream().anyMatch(card -> card == otherCharacter));
	}

	@Test
	@DisplayName("Detecta si una carta coincide con algún elemento de la sugerencia")
	void shouldMatchCardsAgainstSuggestion() {
		CharacterType character = CharacterType.values()[0];
		WeaponType weapon = WeaponType.values()[0];
		RoomType room = RoomType.values()[0];

		assertTrue(cardService.matchesSuggestion(new CharacterCard(character), character, weapon, room));
		assertTrue(cardService.matchesSuggestion(new WeaponCard(weapon), character, weapon, room));
		assertTrue(cardService.matchesSuggestion(new RoomCard(room), character, weapon, room));
		assertFalse(cardService.matchesSuggestion(
				new CharacterCard(CharacterType.values()[1]), character, weapon, room));
	}

	@Test
	@DisplayName("Con dos jugadores no crea el mazo de cartas bonus")
	void shouldNotCreateBonusDeckForTwoPlayers() {
		players(2);

		cardService.createBonusDeck(game);

		verify(bonusDeckRepository, never()).save(any(BonusDeck.class));
		verify(bonusCardRepository, never()).saveAll(any());
	}

	@Test
	@DisplayName("Crea un mazo aleatorio con exactamente las seis cartas bonus")
	void shouldCreateShuffledDeckWithAllSixBonusCards() {
		cardService.createBonusDeck(game);

		ArgumentCaptor<List<BonusCard>> captor = ArgumentCaptor.forClass(List.class);
		verify(bonusCardRepository).saveAll(captor.capture());
		List<BonusCard> cards = captor.getValue();

		assertEquals(6, cards.size());
		assertEquals(
				java.util.Set.of(
						BonusCardEffect.PEEK_CARD,
						BonusCardEffect.EXTRA_DICE_6,
						BonusCardEffect.EXTRA_TURN,
						BonusCardEffect.TELEPORT_ANY_ROOM,
						BonusCardEffect.EXTRA_QUESTION,
						BonusCardEffect.REVEAL_CARD),
				cards.stream().map(BonusCard::getEffect).collect(java.util.stream.Collectors.toSet()));
		assertEquals(
				java.util.Set.of(
						BonusCardEffect.TELEPORT_ANY_ROOM,
						BonusCardEffect.EXTRA_QUESTION,
						BonusCardEffect.REVEAL_CARD),
				cards.stream().filter(BonusCard::isImmediate).map(BonusCard::getEffect)
						.collect(java.util.stream.Collectors.toSet()));
		assertEquals(6, cards.stream().map(BonusCard::getDeckOrder).distinct().count());
	}
}
