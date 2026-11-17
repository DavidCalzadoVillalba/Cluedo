package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;

public interface BonusCardRevealRepository extends CrudRepository<BonusCardReveal, Integer> {

	List<BonusCardReveal> findByGameAndRevealedCardIsNotNull(Game game);

	List<BonusCardReveal> findByGameAndTargetAndRevealedCardIsNull(Game game, Player target);

	Optional<BonusCardReveal> findByIdAndGame(Integer id, Game game);
}
