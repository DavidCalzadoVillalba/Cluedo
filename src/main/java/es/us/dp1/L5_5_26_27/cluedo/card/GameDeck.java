package es.us.dp1.L5_5_26_27.cluedo.card;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "game_decks")
public class GameDeck extends BaseEntity {

	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "game_id", unique = true)
	private Game game;

	@OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GameCard> cards = new ArrayList<>();

}
