package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import jakarta.validation.constraints.NotNull;

public record ExtraQuestionRequest(
		@NotNull Integer bonusCardId,
		@NotNull CharacterType character,
		@NotNull WeaponType weapon,
		@NotNull RoomType room) {
}
