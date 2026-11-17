package es.us.dp1.L5_5_26_27.cluedo.game;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.card.Solution;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

public record GameSolutionResponse(
		CharacterType character,
		WeaponType weapon,
		RoomType room) {

	public static GameSolutionResponse from(Solution solution) {
		return new GameSolutionResponse(
				solution.getCharacterSolution(),
				solution.getWeaponSolution(),
				solution.getRoomSolution());
	}
}
