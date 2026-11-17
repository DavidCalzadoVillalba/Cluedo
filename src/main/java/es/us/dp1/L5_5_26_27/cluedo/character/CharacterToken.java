package es.us.dp1.L5_5_26_27.cluedo.character;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "character_tokens", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "game_id", "character_type" })
})
public class CharacterToken extends BaseEntity {

	@NotNull
	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "character_type", nullable = false)
	private CharacterType character;

	@ManyToOne
	@JoinColumn(name = "current_box_id")
	private Box currentBox;

}
