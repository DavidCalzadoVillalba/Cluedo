package es.us.dp1.L5_5_26_27.cluedo.game;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import es.us.dp1.L5_5_26_27.cluedo.user.User;

import org.springframework.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<Game, Integer> {

	Optional<Game> findByCode(String code);

	List<Game> findAll();

	boolean existsByCode(String code);

	boolean existsByNameIgnoreCaseAndStatus(String name, GameStatus status);

	@Query("select distinct g from Game g join g.players p where p.user = :user and g.status = :status order by g.endedAt desc")
	List<Game> findHistoryByUserAndStatus(@Param("user") User user, @Param("status") GameStatus status);

}
