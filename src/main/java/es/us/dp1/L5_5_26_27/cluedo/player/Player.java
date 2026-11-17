package es.us.dp1.L5_5_26_27.cluedo.player;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.notebook.NotebookMark;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "players", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "game_id", "user_id" }),
		@UniqueConstraint(columnNames = { "game_id", "character_type" })
})
public class Player extends BaseEntity {

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "game_id")
	private Game game;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "character_type", nullable = false)
	private CharacterType character;

	@Column(name = "turn_order")
	private Integer turnOrder;

	@Column(name = "initial_dice_result")
	private Integer initialDiceResult;

	@Column(name = "initial_dice_one")
	private Integer initialDiceOne;

	@Column(name = "initial_dice_two")
	private Integer initialDiceTwo;

	@Column(nullable = false)
	private boolean eliminated;

	@ManyToOne
	@JoinColumn(name = "current_tile_id")
	private Box currentBox;

	@Enumerated(EnumType.STRING)
	@Column(name = "last_suggestion_room")
	private RoomType lastSuggestionRoom;

	@JsonIgnore
	@OneToMany(mappedBy = "owner")
	private List<GameCard> hand = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "holder")
	private List<BonusCard> bonusCards = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "player")
	private List<NotebookMark> notebookMarks = new ArrayList<>();

	@JsonIgnore
	public Game getGame() {
		return game;
	}

	public boolean isHost() {
		return game != null && user != null && game.getHost() != null
				&& user.getId().equals(game.getHost().getId());
	}

}
