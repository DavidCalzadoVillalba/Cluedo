package es.us.dp1.L5_5_26_27.cluedo.card;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.player.Player;

public interface GameCardRepository extends CrudRepository<GameCard, Integer> {

	List<GameCard> findByOwner(Player owner);

	List<GameCard> findByDeckAndOwnerIsNullAndHiddenFalse(GameDeck deck);

	List<GameCard> findByDeckAndOwnerIsNullAndHiddenTrue(GameDeck deck);

}
