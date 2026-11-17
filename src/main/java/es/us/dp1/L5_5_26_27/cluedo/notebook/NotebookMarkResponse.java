package es.us.dp1.L5_5_26_27.cluedo.notebook;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

public record NotebookMarkResponse(
		Integer id,
		NotebookCategory category,
		CharacterType character,
		WeaponType weapon,
		RoomType room,
		NotebookMarkValue value) {

	public static NotebookMarkResponse from(NotebookMark mark) {
		return new NotebookMarkResponse(
				mark.getId(),
				mark.getCategory(),
				mark.getCharacter(),
				mark.getWeapon(),
				mark.getRoom(),
				mark.getValue());
	}
}
