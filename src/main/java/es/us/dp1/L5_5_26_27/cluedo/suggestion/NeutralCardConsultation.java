package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "neutral_card_consultations", uniqueConstraints = @UniqueConstraint(columnNames = { "player_id", "turn_id" }))
public class NeutralCardConsultation extends BaseEntity {

	@ManyToOne(optional = false)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@ManyToOne(optional = false)
	@JoinColumn(name = "card_id", nullable = false)
	private GameCard card;

	@ManyToOne(optional = false)
	@JoinColumn(name = "turn_id", nullable = false)
	private Turn turn;
}
