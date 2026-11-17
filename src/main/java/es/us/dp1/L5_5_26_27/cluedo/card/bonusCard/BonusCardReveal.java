package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bonus_card_reveals")
public class BonusCardReveal extends BaseEntity {

	@NotNull
	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "requester_player_id", nullable = false)
	private Player requester;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "target_player_id", nullable = false)
	private Player target;

	@ManyToOne
	@JoinColumn(name = "revealed_card_id")
	private GameCard revealedCard;
}
