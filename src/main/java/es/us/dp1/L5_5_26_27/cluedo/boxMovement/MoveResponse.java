package es.us.dp1.L5_5_26_27.cluedo.boxMovement;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;

public record MoveResponse(
		Integer boxId,
		int x,
		int y,
		BoxType type,
		RoomType room,
		String message) {
}
