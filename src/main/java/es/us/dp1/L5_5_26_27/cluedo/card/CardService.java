package es.us.dp1.L5_5_26_27.cluedo.card;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardReveal;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRevealResponse;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardService;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

@Service
public class CardService {

	private static final int TWO_PLAYERS_COUNT = 2;

	private final GameCardRepository gameCardRepository;
	private final GameDeckRepository gameDeckRepository;
	private final GameCardDealer cardDealer;
	private final BonusCardService bonusCardService;

	@Autowired
	public CardService(
			GameCardRepository gameCardRepository,
			GameDeckRepository gameDeckRepository,
			GameCardDealer cardDealer,
			BonusCardService bonusCardService) {
		this.gameCardRepository = gameCardRepository;
		this.gameDeckRepository = gameDeckRepository;
		this.cardDealer = cardDealer;
		this.bonusCardService = bonusCardService;
	}

	@Transactional
	public void createAndDealCards(Game game) {
		cardDealer.createAndDealCards(game);
	}

	@Transactional
	public void createBonusDeck(Game game) {
		bonusCardService.createBonusDeck(game);
	}

	@Transactional(readOnly = true)
	public List<GameCard> getHandForPlayer(Player player) {
		return gameCardRepository.findByOwner(player);
	}

	@Transactional(readOnly = true)
	public List<BonusCard> getBonusHandForPlayer(Player player) {
		return bonusCardService.getBonusHandForPlayer(player);
	}

	@Transactional
	public BonusCard drawBonusCard(Game game, Player player) {
		return bonusCardService.drawBonusCard(game, player);
	}

	@Transactional(readOnly = true)
	public BonusCard findBonusCardForPlayer(Integer cardId, Player player) {
		return bonusCardService.findBonusCardForPlayer(cardId, player);
	}

	@Transactional
	public void discardBonusCard(BonusCard card) {
		bonusCardService.discardBonusCard(card);
	}

	@Transactional
	public BonusCardReveal createPublicReveal(Game game, Player requester, Integer targetPlayerId, BonusCard card) {
		return bonusCardService.createPublicReveal(game, requester, targetPlayerId, card);
	}

	@Transactional
	public BonusCardReveal revealCardToEveryone(Integer revealId, Game game, Player target, Integer cardId) {
		return bonusCardService.revealCardToEveryone(revealId, game, target, cardId);
	}

	@Transactional(readOnly = true)
	public List<BonusCardRevealResponse> getPendingPublicReveals(Game game, Player target) {
		return bonusCardService.getPendingPublicReveals(game, target);
	}

	@Transactional(readOnly = true)
	public List<BonusCardRevealResponse> getPublicReveals(Game game) {
		return bonusCardService.getPublicReveals(game);
	}

	@Transactional(readOnly = true)
	public List<GameCard> getFaceUpCards(Game game) {
		GameDeck deck = gameDeckRepository.findByGame(game).orElse(null);
		if (deck == null) {
			return List.of();
		}
		return gameCardRepository.findByDeckAndOwnerIsNullAndHiddenFalse(deck);
	}

	@Transactional(readOnly = true)
	public List<GameCard> getHiddenCardsMatching(Game game, CharacterType character, WeaponType weapon, RoomType room) {
		if (game.getPlayers().size() != TWO_PLAYERS_COUNT) {
			return List.of();
		}
		GameDeck deck = gameDeckRepository.findByGame(game)
				.orElseThrow(() -> new ResourceNotFoundException("Mazo", "partida", game.getCode()));
		return gameCardRepository.findByDeckAndOwnerIsNullAndHiddenTrue(deck).stream()
				.filter(card -> matchesSuggestion(card, character, weapon, room))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<GameCard> getHiddenCards(Game game) {
		if (game.getPlayers().size() != TWO_PLAYERS_COUNT) {
			return List.of();
		}
	GameDeck deck = gameDeckRepository.findByGame(game)
				.orElseThrow(() -> new ResourceNotFoundException("Mazo", "partida", game.getCode()));
		return gameCardRepository.findByDeckAndOwnerIsNullAndHiddenTrue(deck);
	}

	@Transactional(readOnly = true)
	public Optional<GameCard> findCard(Integer cardId) {
		return gameCardRepository.findById(cardId);
	}

	@Transactional(readOnly = true)
	public List<GameCard> getMatchingCardsForPlayer(Player player, CharacterType character, WeaponType weapon,
			RoomType room) {
		return getHandForPlayer(player).stream()
				.filter(card -> matchesSuggestion(card, character, weapon, room))
				.toList();
	}

	public boolean matchesSuggestion(GameCard card, CharacterType character, WeaponType weapon, RoomType room) {
		return cardDealer.matchesSuggestion(card, character, weapon, room);
	}
}
