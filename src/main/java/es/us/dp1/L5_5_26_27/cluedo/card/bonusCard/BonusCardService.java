package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCardRepository;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.CardNotOwnedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;

@Service
public class BonusCardService {

	private static final int TWO_PLAYERS_COUNT = 2;
	private static final int INITIAL_DECK_ORDER = 0;

	private final BonusCardRepository bonusCardRepository;
	private final BonusDeckRepository bonusDeckRepository;
	private final BonusCardRevealRepository bonusCardRevealRepository;
	private final GameCardRepository gameCardRepository;
	private final Random random;

	@Autowired
	public BonusCardService(
			BonusCardRepository bonusCardRepository,
			BonusDeckRepository bonusDeckRepository,
			BonusCardRevealRepository bonusCardRevealRepository,
			GameCardRepository gameCardRepository) {
		this(bonusCardRepository, bonusDeckRepository, bonusCardRevealRepository, gameCardRepository, new Random());
	}

	public BonusCardService(
			BonusCardRepository bonusCardRepository,
			BonusDeckRepository bonusDeckRepository,
			BonusCardRevealRepository bonusCardRevealRepository,
			GameCardRepository gameCardRepository,
			Random random) {
		this.bonusCardRepository = bonusCardRepository;
		this.bonusDeckRepository = bonusDeckRepository;
		this.bonusCardRevealRepository = bonusCardRevealRepository;
		this.gameCardRepository = gameCardRepository;
		this.random = random;
	}

	@Transactional
	public void createBonusDeck(Game game) {
		if (game.getPlayers().size() == TWO_PLAYERS_COUNT) {
			return;
		}

		BonusDeck bonusDeck = new BonusDeck();
		bonusDeck.setGame(game);
		bonusDeck = bonusDeckRepository.save(bonusDeck);

		List<BonusCard> cards = new ArrayList<>();
		cards.add(new BonusCard(BonusCardEffect.PEEK_CARD, false, INITIAL_DECK_ORDER));
		cards.add(new BonusCard(BonusCardEffect.EXTRA_DICE_6, false, INITIAL_DECK_ORDER));
		cards.add(new BonusCard(BonusCardEffect.EXTRA_TURN, false, INITIAL_DECK_ORDER));
		cards.add(new BonusCard(BonusCardEffect.TELEPORT_ANY_ROOM, true, INITIAL_DECK_ORDER));
		cards.add(new BonusCard(BonusCardEffect.EXTRA_QUESTION, true, INITIAL_DECK_ORDER));
		cards.add(new BonusCard(BonusCardEffect.REVEAL_CARD, true, INITIAL_DECK_ORDER));

		Collections.shuffle(cards, random);

		for (int i = 0; i < cards.size(); i++) {
			BonusCard card = cards.get(i);
			card.setDeck(bonusDeck);
			card.setDeckOrder(i);
		}

		bonusCardRepository.saveAll(cards);
	}

	@Transactional(readOnly = true)
	public List<BonusCard> getBonusHandForPlayer(Player player) {
		return bonusCardRepository.findByHolderAndUsedFalse(player);
	}

	@Transactional
	public BonusCard drawBonusCard(Game game, Player player) {
		if (game.getPlayers().size() == TWO_PLAYERS_COUNT) {
			return null;
		}

		BonusDeck deck = bonusDeckRepository.findByGame(game).orElse(null);
		if (deck == null) {
			createBonusDeck(game);
			deck = bonusDeckRepository.findByGame(game)
					.orElseThrow(() -> new IllegalStateException("No se pudo crear el mazo bonus de la partida"));
		}

		Optional<BonusCard> nextCard = bonusCardRepository
				.findByDeckAndHolderIsNullAndUsedFalseOrderByDeckOrderAsc(deck).stream().findFirst();
		if (nextCard.isEmpty()) {
			return null;
		}

		BonusCard card = nextCard.get();
		card.setHolder(player);
		return bonusCardRepository.save(card);
	}

	@Transactional(readOnly = true)
	public BonusCard findBonusCardForPlayer(Integer cardId, Player player) {
		BonusCard card = bonusCardRepository.findById(cardId)
				.orElseThrow(() -> new ResourceNotFoundException("Carta bonus", "id", cardId));
		if (card.isUsed() || card.getHolder() == null || !card.getHolder().getId().equals(player.getId())) {
			throw new GameRuleException("La carta bonus no pertenece a tu mano");
		}
		return card;
	}

	@Transactional
	public void discardBonusCard(BonusCard card) {
		card.setHolder(null);
		card.setUsed(true);
		bonusCardRepository.save(card);
	}

	@Transactional
	public BonusCardReveal createPublicReveal(Game game, Player requester, Integer targetPlayerId, BonusCard card) {
		if (card.getEffect() != BonusCardEffect.REVEAL_CARD) {
			throw new GameRuleException("La carta no permite solicitar que otro jugador revele una carta");
		}
		Player target = game.getPlayers().stream()
				.filter(candidate -> candidate.getId().equals(targetPlayerId))
				.findFirst()
				.orElseThrow(() -> new GameRuleException("El jugador elegido no pertenece a la partida"));
		if (target.isEliminated()) {
			throw new GameRuleException("No puedes elegir a un jugador eliminado");
		}
		if (target.getId().equals(requester.getId())) {
			throw new GameRuleException("Debes elegir a otro jugador");
		}

		BonusCardReveal reveal = new BonusCardReveal();
		reveal.setGame(game);
		reveal.setRequester(requester);
		reveal.setTarget(target);
		bonusCardRevealRepository.save(reveal);
		return reveal;
	}

	@Transactional
	public BonusCardReveal revealCardToEveryone(Integer revealId, Game game, Player target, Integer cardId) {
		BonusCardReveal reveal = bonusCardRevealRepository.findByIdAndGame(revealId, game)
				.orElseThrow(() -> new GameRuleException("No hay una solicitud de revelación pendiente"));
		if (!reveal.getTarget().getId().equals(target.getId())) {
			throw new GameRuleException("Esta solicitud no está dirigida a ti");
		}
		if (reveal.getRevealedCard() != null) {
			throw new GameRuleException("La solicitud ya ha sido resuelta");
		}
		GameCard card = gameCardRepository.findById(cardId)
				.orElseThrow(() -> new ResourceNotFoundException("Carta", "id", cardId));
		if (card.getOwner() == null || !card.getOwner().getId().equals(target.getId())) {
			throw new CardNotOwnedException();
		}

		reveal.setRevealedCard(card);
		return bonusCardRevealRepository.save(reveal);
	}

	@Transactional(readOnly = true)
	public List<BonusCardRevealResponse> getPendingPublicReveals(Game game, Player target) {
		return bonusCardRevealRepository.findByGameAndTargetAndRevealedCardIsNull(game, target).stream()
				.map(BonusCardRevealResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<BonusCardRevealResponse> getPublicReveals(Game game) {
		return bonusCardRevealRepository.findByGameAndRevealedCardIsNotNull(game).stream()
				.map(BonusCardRevealResponse::from)
				.toList();
	}
}
