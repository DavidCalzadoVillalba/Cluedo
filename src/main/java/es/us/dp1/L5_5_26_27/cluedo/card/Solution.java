package es.us.dp1.L5_5_26_27.cluedo.card;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solutions")
public class Solution extends BaseEntity {

	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "game_id", unique = true)
	private Game game;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "room_solution", nullable = false)
	private RoomType roomSolution;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_solution", nullable = false)
	private WeaponType weaponSolution;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "character_solution", nullable = false)
	private CharacterType characterSolution;

}
