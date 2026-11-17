package es.us.dp1.L5_5_26_27.cluedo.turn;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;

public interface TurnRepository extends CrudRepository<Turn, Integer> {

	Optional<Turn> findFirstByGameAndStatusOrderByTurnNumberDesc(Game game, TurnStatus status);

	List<Turn> findByStatusAndExpiresAtLessThanEqual(TurnStatus status, Instant expiresAt);
}
