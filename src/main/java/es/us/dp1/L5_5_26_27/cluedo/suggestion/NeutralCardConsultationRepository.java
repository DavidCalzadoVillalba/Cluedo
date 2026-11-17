package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;

public interface NeutralCardConsultationRepository extends CrudRepository<NeutralCardConsultation, Integer> {
	List<NeutralCardConsultation> findByPlayer(Player player);

	Optional<NeutralCardConsultation> findByPlayerAndCard(Player player, GameCard card);

	Optional<NeutralCardConsultation> findByPlayerAndTurn(Player player, Turn turn);
}
