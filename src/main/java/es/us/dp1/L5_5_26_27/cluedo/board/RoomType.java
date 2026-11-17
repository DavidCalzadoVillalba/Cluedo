package es.us.dp1.L5_5_26_27.cluedo.board;

public enum RoomType {
	KITCHEN,
	DINING_ROOM,
	LOUNGE,
	PATIO,
	GAME_ROOM,
	STUDY,
	BEDROOM,
	BATHROOM,
	GARAGE,
	CENTER;

	public boolean isCardRoom() {
		return this != CENTER;
	}

	public boolean isAccusationRoom() {
		return this == CENTER;
	}
}
