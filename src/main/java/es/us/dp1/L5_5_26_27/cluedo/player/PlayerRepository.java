package es.us.dp1.L5_5_26_27.cluedo.player;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

	Optional<Player> findByGameAndUser(Game game, User user);

	boolean existsByGameAndCharacter(Game game, CharacterType character);

	Optional<Player> findByGameAndCharacter(Game game, CharacterType character);

	List<Player> findByGameOrderByIdAsc(Game game);

	@Query("SELECT COUNT(p) FROM Player p WHERE p.user.id = :userId AND p.game.status = es.us.dp1.L5_5_26_27.cluedo.game.GameStatus.ENDED")
	long countFinishedGamesByUserId(@Param("userId") Integer userId);

	@Query("SELECT COUNT(p) FROM Player p WHERE p.user.id = :userId AND p.game.status = es.us.dp1.L5_5_26_27.cluedo.game.GameStatus.ENDED AND p.game.winner.user.id = :userId")
	long countWinsByUserId(@Param("userId") Integer userId);

}
