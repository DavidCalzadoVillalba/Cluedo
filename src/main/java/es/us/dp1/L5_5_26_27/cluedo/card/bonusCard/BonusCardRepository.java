package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.player.Player;

public interface BonusCardRepository extends CrudRepository<BonusCard, Integer> {

	List<BonusCard> findByDeckAndHolderIsNullAndUsedFalseOrderByDeckOrderAsc(BonusDeck deck);

	List<BonusCard> findByHolderAndUsedFalse(Player holder);

}
