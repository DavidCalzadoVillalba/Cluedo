package es.us.dp1.L5_5_26_27.cluedo.card;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;

public interface SolutionRepository extends CrudRepository<Solution, Integer> {

	Optional<Solution> findByGame(Game game);

}
