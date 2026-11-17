package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "suggestions")
public class Suggestion extends BaseEntity {

	@NotNull
	@JsonIgnore
	@OneToOne(optional = false)
	@JoinColumn(name = "turn_id", nullable = false, unique = true)
	private Turn turn;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RoomType room;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "character_type", nullable = false)
	private CharacterType character;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_type", nullable = false)
	private WeaponType weapon;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SuggestionStatus status;

	@Column(name = "final_accusation", nullable = false)
	private boolean finalAccusation;

	@ManyToOne
	@JoinColumn(name = "current_responder_id")
	private Player currentResponder;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "peeked_by_player_id")
	private Player peekedBy;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "shown_card_id")
	private GameCard shownCard;

}
