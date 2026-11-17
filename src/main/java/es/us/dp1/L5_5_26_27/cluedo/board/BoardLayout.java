package es.us.dp1.L5_5_26_27.cluedo.board;

import java.util.List;
import java.util.Map;
import java.util.Set;

import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;

public final class BoardLayout {

	public static final int WIDTH = 21;
	public static final int HEIGHT = 19;

	private BoardLayout() {
	}

	public record Coord(int x, int y) {
	}

	public record SecretPassage(Coord from, Coord to, RoomType room) {
	}

	public record CellRange(int start, int end) {
		public boolean contains(int cellId) {
			return cellId >= start && cellId <= end;
		}
	}

	public static final Map<RoomType, List<CellRange>> ROOM_CELLS = Map.ofEntries(
			Map.entry(RoomType.BEDROOM, List.of(
					new CellRange(3, 5), new CellRange(24, 26), new CellRange(44, 48),
					new CellRange(65, 69), new CellRange(86, 90), new CellRange(107, 112),
					new CellRange(128, 133))),
			Map.entry(RoomType.BATHROOM, List.of(
					new CellRange(7, 10), new CellRange(28, 31), new CellRange(49, 52))),
			Map.entry(RoomType.STUDY, List.of(
					new CellRange(32, 35), new CellRange(53, 56), new CellRange(74, 77),
					new CellRange(95, 98))),
			Map.entry(RoomType.GAME_ROOM, List.of(
					new CellRange(150, 153), new CellRange(170, 174), new CellRange(191, 195),
					new CellRange(212, 216), new CellRange(233, 237), new CellRange(255, 258))),
			Map.entry(RoomType.KITCHEN, List.of(
					new CellRange(36, 41), new CellRange(57, 62), new CellRange(78, 83),
					new CellRange(99, 104), new CellRange(120, 125), new CellRange(141, 146))),
			Map.entry(RoomType.GARAGE, List.of(
					new CellRange(274, 279), new CellRange(295, 300), new CellRange(316, 321),
					new CellRange(337, 342), new CellRange(360, 363))),
			Map.entry(RoomType.DINING_ROOM, List.of(
					new CellRange(161, 168), new CellRange(182, 189), new CellRange(203, 210),
					new CellRange(224, 231), new CellRange(245, 252))),
			Map.entry(RoomType.LOUNGE, List.of(
					new CellRange(267, 272), new CellRange(288, 293), new CellRange(309, 314),
					new CellRange(330, 335), new CellRange(351, 356), new CellRange(372, 377))),
			Map.entry(RoomType.CENTER, List.of(
					new CellRange(198, 201), new CellRange(219, 222), new CellRange(240, 243))),
			Map.entry(RoomType.PATIO, List.of(
					new CellRange(322, 323), new CellRange(328, 329), new CellRange(343, 350),
					new CellRange(364, 371))));

	static final List<Coord> DOORS = List.of(
			// Garaje: Casilla 300 (x=5, y=14)
			new Coord(5, 14),
			// Salón: Casilla 288 (x=14, y=13)
			new Coord(14, 13),
			// Comedor: Casilla 203 (x=13, y=9) y conexión a cocina 164 (x=16, y=7), 165
			// (x=17, y=7)
			new Coord(13, 9), new Coord(16, 7), new Coord(17, 7),
			// Cocina: Casilla 120 (x=14, y=5) y conexión a comedor 143 (x=16, y=6), 144
			// (x=17, y=6)
			new Coord(14, 5), new Coord(16, 6), new Coord(17, 6),
			// Baño: Casilla 51 (x=8, y=2) y conexión a dormitorio 49 (x=6, y=2)
			new Coord(8, 2), new Coord(6, 2),
			// Estudio: Casilla 74 (x=10, y=3)
			new Coord(10, 3),
			// Dormitorio: Casilla 90 (x=5, y=4) y conexión a baño 48 (x=5, y=2)
			new Coord(5, 4), new Coord(5, 2),
			// Sala de juego: Casilla 216 (x=5, y=10)
			new Coord(5, 10),
			// Patio: Casilla 346 (x=9, y=16), 347 (x=10, y=16)
			new Coord(9, 16), new Coord(10, 16),
			// Centro: Norte (10, 9), Sur (10, 11), Oeste (8, 10), Este (11, 10)
			new Coord(10, 9), new Coord(10, 11), new Coord(8, 10), new Coord(11, 10));

	public static final Set<Integer> UNCLICKABLE_CELL_IDS = Set.of(
			1, 2, 6,
			11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
			22, 23, 27, 42, 63, 84, 105, 126, 147, 273, 294, 315, 336, 357, 378, 358, 359,
			43, 64, 85, 106, 127, 148, 149, 169, 190, 211, 232, 253, 254,
			115, 136,
			379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 393, 394, 395, 396, 397, 398, 399);

	public static boolean isUnclickable(int x, int y) {
		int sequentialId = y * WIDTH + x + 1;
		return UNCLICKABLE_CELL_IDS.contains(sequentialId);
	}

	static final List<Coord> BONUS_CELLS = List.of(
			new Coord(10, 5),
			new Coord(7, 7),
			new Coord(9, 12),
			new Coord(11, 14),
			new Coord(12, 8));

	public static boolean isBonusCell(int x, int y) {
		return BONUS_CELLS.contains(new Coord(x, y));
	}

	static final List<SecretPassage> SECRET_PASSAGES = List.of(
			new SecretPassage(new Coord(17, 3), new Coord(2, 16), RoomType.KITCHEN),
			new SecretPassage(new Coord(2, 16), new Coord(17, 3), RoomType.GARAGE),
			new SecretPassage(new Coord(2, 3), new Coord(17, 15), RoomType.BEDROOM),
			new SecretPassage(new Coord(17, 15), new Coord(2, 3), RoomType.LOUNGE));

	public static final Map<CharacterType, Coord> CHARACTER_STARTS = Map.of(
			CharacterType.AMAPOLA, new Coord(9, 9),
			CharacterType.RUBIO, new Coord(10, 9),
			CharacterType.BLANCO, new Coord(11, 9),
			CharacterType.PRADO, new Coord(9, 11),
			CharacterType.CELESTE, new Coord(10, 10),
			CharacterType.MORA, new Coord(11, 11));

	public static final Map<WeaponType, RoomType> WEAPON_START_ROOMS = Map.of(
			WeaponType.CANDELABRO, RoomType.KITCHEN,
			WeaponType.CUCHILLO, RoomType.DINING_ROOM,
			WeaponType.TUBERIA_PLOMO, RoomType.LOUNGE,
			WeaponType.PISTOLA, RoomType.GAME_ROOM,
			WeaponType.CUERDA, RoomType.STUDY,
			WeaponType.LLAVE_INGLESA, RoomType.BEDROOM);

	public static RoomType findRoomAt(int x, int y) {
		int cellId = y * WIDTH + x + 1;
		for (Map.Entry<RoomType, List<CellRange>> entry : ROOM_CELLS.entrySet()) {
			for (CellRange range : entry.getValue()) {
				if (range.contains(cellId)) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

	public static Coord roomCenter(RoomType room) {
		return switch (room) {
			case BEDROOM -> new Coord(2, 3);
			case BATHROOM -> new Coord(8, 1);
			case STUDY -> new Coord(11, 2);
			case KITCHEN -> new Coord(17, 3);
			case GAME_ROOM -> new Coord(2, 10);
			case DINING_ROOM -> new Coord(17, 10);
			case GARAGE -> new Coord(2, 16);
			case LOUNGE -> new Coord(17, 15);
			case PATIO -> new Coord(9, 17);
			case CENTER -> new Coord(10, 10);
		};
	}

	public static boolean isInAccusationRoom(Box box) {
		return box != null && box.getRoom() != null && box.getRoom().isAccusationRoom();
	}
}
