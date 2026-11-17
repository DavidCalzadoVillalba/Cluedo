package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;

public interface BonusDeckRepository extends CrudRepository<BonusDeck, Integer> {

	Optional<BonusDeck> findByGame(Game game);

}
