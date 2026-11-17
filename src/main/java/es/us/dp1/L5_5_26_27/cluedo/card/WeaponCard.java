package es.us.dp1.L5_5_26_27.cluedo.card;

import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
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
@DiscriminatorValue("WEAPON")
public class WeaponCard extends GameCard {

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_type")
	private WeaponType weapon;

	public WeaponCard(WeaponType weapon) {
		this.weapon = weapon;
	}

}
