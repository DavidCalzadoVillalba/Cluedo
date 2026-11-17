package es.us.dp1.L5_5_26_27.cluedo.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_cards")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "card_type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "cardType")
@JsonSubTypes({
	@JsonSubTypes.Type(value = RoomCard.class, name = "ROOM"),
	@JsonSubTypes.Type(value = WeaponCard.class, name = "WEAPON"),
	@JsonSubTypes.Type(value = CharacterCard.class, name = "CHARACTER")
})
public abstract class GameCard extends BaseEntity {

	@Column(name = "hidden", nullable = false)
	private boolean hidden;

	@ManyToOne
	@JoinColumn(name = "deck_id")
	@JsonIgnore
	private GameDeck deck;

	@ManyToOne
	@JoinColumn(name = "owner_id")
	@JsonIgnore
	private Player owner;

}
