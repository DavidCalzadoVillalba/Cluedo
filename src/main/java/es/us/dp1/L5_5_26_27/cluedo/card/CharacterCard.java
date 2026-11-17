package es.us.dp1.L5_5_26_27.cluedo.card;

import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
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
@DiscriminatorValue("CHARACTER")
public class CharacterCard extends GameCard {

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "character_type")
	private CharacterType character;

	public CharacterCard(CharacterType character) {
		this.character = character;
	}

}
