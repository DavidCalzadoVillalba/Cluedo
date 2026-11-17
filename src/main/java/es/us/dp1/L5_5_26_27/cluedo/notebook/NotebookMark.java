package es.us.dp1.L5_5_26_27.cluedo.notebook;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
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
@Table(name = "notebook_marks", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "player_id", "character_type" }),
		@UniqueConstraint(columnNames = { "player_id", "weapon_type" }),
		@UniqueConstraint(columnNames = { "player_id", "room_type" })
})
public class NotebookMark extends BaseEntity {

	@NotNull
	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotebookCategory category;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "mark_value", nullable = false)
	private NotebookMarkValue value;

	@Enumerated(EnumType.STRING)
	@Column(name = "character_type")
	private CharacterType character;

	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_type")
	private WeaponType weapon;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_type")
	private RoomType room;

}
