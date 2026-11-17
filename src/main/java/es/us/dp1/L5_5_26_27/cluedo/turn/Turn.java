package es.us.dp1.L5_5_26_27.cluedo.turn;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.suggestion.Suggestion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "turns")
public class Turn extends BaseEntity {

	@NotNull
	@Min(1)
	@Column(name = "turn_number", nullable = false)
	private Integer turnNumber;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TurnStatus status;

	@Min(0)
	@Column(name = "dice_result")
	private Integer diceResult;

	@Column(name = "dice_one")
	private Integer diceOne;

	@Column(name = "dice_two")
	private Integer diceTwo;

	@Column(name = "has_moved", nullable = false)
	private boolean hasMoved = false;

	@Column(name = "movement_bonus", nullable = false)
	private int movementBonus;

	@Column(name = "extra_turn", nullable = false)
	private boolean extraTurn;

	@NotNull
	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Transient
	public Long getRemainingSeconds() {
		if (expiresAt == null) {
			return null;
		}
		long seconds = Duration.between(Instant.now(), expiresAt).getSeconds();
		return Math.max(0, seconds);
	}

	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;

	@ManyToOne(optional = false)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@OneToOne(mappedBy = "turn")
	private Suggestion suggestion;
}
