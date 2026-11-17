package es.us.dp1.L5_5_26_27.cluedo.board;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;

public interface BoardRepository extends CrudRepository<Board, Integer> {

	Optional<Board> findByGame(Game game);
}
