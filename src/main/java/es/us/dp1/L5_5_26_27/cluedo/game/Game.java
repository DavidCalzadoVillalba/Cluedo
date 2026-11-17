package es.us.dp1.L5_5_26_27.cluedo.game;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.us.dp1.L5_5_26_27.cluedo.character.CharacterToken;
import es.us.dp1.L5_5_26_27.cluedo.board.Board;
import es.us.dp1.L5_5_26_27.cluedo.card.GameDeck;
import es.us.dp1.L5_5_26_27.cluedo.card.Solution;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusDeck;
import es.us.dp1.L5_5_26_27.cluedo.model.BaseEntity;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.turn.Turn;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "games")
public class Game extends BaseEntity {

	@NotBlank
	@Size(min = 3, max = 40)
	@Column(nullable = false)
	private String name;

	@Column(unique = true, nullable = false, length = 6)
	private String code;

	@NotNull
	@Min(2)
	@Max(6)
	@Column(name = "max_players", nullable = false)
	private Integer maxPlayers;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GameStatus status;

	@Column(name = "ended_at")
	private Instant endedAt;

	@ManyToOne(optional = false)
	@JoinColumn(name = "host_id")
	private User host;

	@OneToMany(mappedBy = "game")
	@OrderBy("id ASC")
	private List<Player> players = new ArrayList<>();

	@OneToOne(mappedBy = "game", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	private Board board;

	@JsonIgnore
	@OneToMany(mappedBy = "game")
	@OrderBy("turnNumber ASC")
	private List<Turn> turns = new ArrayList<>();

	@JsonIgnore
	@OneToOne(mappedBy = "game", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	private GameDeck gameDeck;

	@JsonIgnore
	@OneToOne(mappedBy = "game", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	private BonusDeck bonusDeck;

	@JsonIgnore
	@OneToOne(mappedBy = "game", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	private Solution solution;

	@ManyToOne
	@JoinColumn(name = "winner_id")
	private Player winner;

	@JsonIgnore
	@OneToMany(mappedBy = "game")
	private List<WeaponToken> weaponTokens = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "game")
	private List<CharacterToken> characterTokens = new ArrayList<>();

}
