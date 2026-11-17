package es.us.dp1.L5_5_26_27.cluedo.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

@Component
public class GameCardDealer {

	private static final int TWO_PLAYERS_COUNT = 2;
	private static final int TWO_PLAYERS_HIDDEN_CARDS_COUNT = 4;
	private static final int SECRET_CARD_INDEX = 0;

	private final GameDeckRepository gameDeckRepository;
	private final SolutionRepository solutionRepository;
	private final GameCardRepository gameCardRepository;
	private final PlayerRepository playerRepository;
	private final Random random;

	@Autowired
	public GameCardDealer(
			GameDeckRepository gameDeckRepository,
			SolutionRepository solutionRepository,
			GameCardRepository gameCardRepository,
			PlayerRepository playerRepository) {
		this(gameDeckRepository, solutionRepository, gameCardRepository, playerRepository, new Random());
	}

	public GameCardDealer(
			GameDeckRepository gameDeckRepository,
			SolutionRepository solutionRepository,
			GameCardRepository gameCardRepository,
			PlayerRepository playerRepository,
			Random random) {
		this.gameDeckRepository = gameDeckRepository;
		this.solutionRepository = solutionRepository;
		this.gameCardRepository = gameCardRepository;
		this.playerRepository = playerRepository;
		this.random = random;
	}

	public void createAndDealCards(Game game) {
		GameDeck gameDeck = new GameDeck();
		gameDeck.setGame(game);
		gameDeck = gameDeckRepository.save(gameDeck);

		List<CharacterCard> characterCards = buildCharacterCards(gameDeck);
		List<WeaponCard> weaponCards = buildWeaponCards(gameDeck);
		List<RoomCard> roomCards = buildRoomCards(gameDeck);

		Collections.shuffle(characterCards, random);
		Collections.shuffle(weaponCards, random);
		Collections.shuffle(roomCards, random);

		CharacterCard secretCharacter = characterCards.remove(SECRET_CARD_INDEX);
		WeaponCard secretWeapon = weaponCards.remove(SECRET_CARD_INDEX);
		RoomCard secretRoom = roomCards.remove(SECRET_CARD_INDEX);

		Solution solution = new Solution();
		solution.setGame(game);
		solution.setCharacterSolution(secretCharacter.getCharacter());
		solution.setWeaponSolution(secretWeapon.getWeapon());
		solution.setRoomSolution(secretRoom.getRoom());
		solutionRepository.save(solution);
		game.setSolution(solution);

		List<GameCard> cardsToDeal = new ArrayList<>();
		cardsToDeal.addAll(characterCards);
		cardsToDeal.addAll(weaponCards);
		cardsToDeal.addAll(roomCards);
		Collections.shuffle(cardsToDeal, random);

		dealCardsToPlayers(game.getPlayers(), cardsToDeal);

		gameCardRepository.saveAll(cardsToDeal);
		if (game.getPlayers() != null) {
			playerRepository.saveAll(game.getPlayers());
		}
	}

	private void dealCardsToPlayers(List<Player> players, List<GameCard> cardsToDeal) {
		if (players == null || players.isEmpty()) {
			return;
		}
		int numPlayers = players.size();

		if (numPlayers == TWO_PLAYERS_COUNT) {
			for (int i = 0; i < TWO_PLAYERS_HIDDEN_CARDS_COUNT; i++) {
				cardsToDeal.get(i).setHidden(true);
			}

			int playerIndex = 0;
			for (int i = TWO_PLAYERS_HIDDEN_CARDS_COUNT; i < cardsToDeal.size(); i++) {
				GameCard card = cardsToDeal.get(i);
				Player owner = players.get(playerIndex % numPlayers);
				card.setOwner(owner);
				owner.getHand().add(card);
				playerIndex++;
			}
		} else {
			int cardsPerPlayer = cardsToDeal.size() / numPlayers;
			int totalDealableCards = cardsPerPlayer * numPlayers;

			for (int i = 0; i < totalDealableCards; i++) {
				Player owner = players.get(i % numPlayers);
				GameCard card = cardsToDeal.get(i);
				card.setOwner(owner);
				owner.getHand().add(card);
			}
		}
	}

	private List<CharacterCard> buildCharacterCards(GameDeck gameDeck) {
		List<CharacterCard> cards = new ArrayList<>();
		for (CharacterType character : CharacterType.values()) {
			CharacterCard card = new CharacterCard(character);
			card.setDeck(gameDeck);
			cards.add(card);
		}
		return cards;
	}

	private List<WeaponCard> buildWeaponCards(GameDeck gameDeck) {
		List<WeaponCard> cards = new ArrayList<>();
		for (WeaponType weapon : WeaponType.values()) {
			WeaponCard card = new WeaponCard(weapon);
			card.setDeck(gameDeck);
			cards.add(card);
		}
		return cards;
	}

	private List<RoomCard> buildRoomCards(GameDeck gameDeck) {
		List<RoomCard> cards = new ArrayList<>();
		for (RoomType room : RoomType.values()) {
			if (!room.isCardRoom()) {
				continue;
			}
			RoomCard card = new RoomCard(room);
			card.setDeck(gameDeck);
			cards.add(card);
		}
		return cards;
	}

	public boolean matchesSuggestion(GameCard card, CharacterType character, WeaponType weapon, RoomType room) {
		return (card instanceof CharacterCard characterCard && characterCard.getCharacter() == character)
				|| (card instanceof WeaponCard weaponCard && weaponCard.getWeapon() == weapon)
				|| (card instanceof RoomCard roomCard && roomCard.getRoom() == room);
	}
}

