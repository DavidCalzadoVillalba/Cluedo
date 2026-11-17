package es.us.dp1.L5_5_26_27.cluedo.boxMovement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.board.Board;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "boxes", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "board_id", "x", "y" })
})
public class Box extends BaseEntity {

	@NotNull
	@Min(0)
	@Column(name = "x", nullable = false)
	private Integer x;

	@NotNull
	@Min(0)
	@Column(name = "y", nullable = false)
	private Integer y;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "box_type", nullable = false)
	private BoxType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_type")
	private RoomType room;

	@NotNull
	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "secret_passage_destination_id")
	private Box secretPassageDestination;
}
