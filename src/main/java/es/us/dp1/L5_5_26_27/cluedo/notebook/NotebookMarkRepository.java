package es.us.dp1.L5_5_26_27.cluedo.notebook;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.player.Player;

public interface NotebookMarkRepository extends CrudRepository<NotebookMark, Integer> {

	List<NotebookMark> findByPlayerOrderByCategoryAscIdAsc(Player player);

	Optional<NotebookMark> findByIdAndPlayer(Integer id, Player player);
}
