package es.us.dp1.L5_5_26_27.cluedo.card;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("ROOM")
public class RoomCard extends GameCard {

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "room_type")
	private RoomType room;

	public RoomCard(RoomType room) {
		this.room = room;
	}

}
