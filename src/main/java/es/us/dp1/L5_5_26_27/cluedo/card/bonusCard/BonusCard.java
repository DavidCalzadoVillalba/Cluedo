package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bonus_cards")
public class BonusCard extends BaseEntity {

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BonusCardEffect effect;

	@Column(nullable = false)
	private boolean immediate; // true: efecto inmediato (RN-14); false: almacenable en mano (RN-14)

	@Column(nullable = false)
	private boolean used; // true: ya ha sido consumida y descartada (RN-15)

	@Column(name = "deck_order")
	private Integer deckOrder;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "deck_id")
	private BonusDeck deck;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "holder_id")
	private Player holder; // null si está en el mazo o descartada

	public BonusCard(BonusCardEffect effect, boolean immediate, Integer deckOrder) {
		this.effect = effect;
		this.immediate = immediate;
		this.deckOrder = deckOrder;
		this.used = false;
	}

}
