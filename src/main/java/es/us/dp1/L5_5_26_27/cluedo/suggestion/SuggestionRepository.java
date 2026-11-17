package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;

public interface SuggestionRepository extends CrudRepository<Suggestion, Integer> {

	Optional<Suggestion> findByTurn(Turn turn);
}
