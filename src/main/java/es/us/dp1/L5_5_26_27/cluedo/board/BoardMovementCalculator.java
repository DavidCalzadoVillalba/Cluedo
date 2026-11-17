package es.us.dp1.L5_5_26_27.cluedo.board;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Component;

import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.BoxType;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.BoardNotInitializedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;

@Component
public class BoardMovementCalculator {

	private static final int[][] DELTAS = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };

	private record Step(Box box, int remaining, Set<Integer> visitedPath) {
	}

	public List<Box> calculateReachableBoxes(Player player, int diceRoll) {
		if (diceRoll <= 0) {
			return List.of();
		}
		Box startBox = player.getCurrentBox();
		if (startBox == null) {
			throw new GameRuleException("El jugador no tiene casilla asignada en el tablero");
		}
		Board board = player.getGame().getBoard();
		if (board == null) {
			throw new BoardNotInitializedException();
		}

		Box[][] grid = buildGridMap(board);
		Set<Integer> blockedBoxIds = findBlockedBoxIds(player);
		RoomType startingRoom = startBox.getRoom();

		Set<Box> reachable = new HashSet<>();
		Queue<Step> queue = new ArrayDeque<>();

		if (startingRoom != null) {
			initQueueFromRoom(board, grid, startingRoom, diceRoll, blockedBoxIds, reachable, queue);
		} else {
			Set<Integer> initialPath = new HashSet<>();
			initialPath.add(startBox.getId());
			queue.add(new Step(startBox, diceRoll, initialPath));
		}

		processBFS(board, grid, startingRoom, blockedBoxIds, reachable, queue);

		reachable.remove(startBox);
		reachable.removeIf(b -> BoardLayout.isUnclickable(b.getX(), b.getY()));
		if (startingRoom != null) {
			// RN-10: No se permite moverse dentro de la misma habitación
			reachable.removeIf(b -> b.getRoom() != null && b.getRoom() == startingRoom);
		}
		return new ArrayList<>(reachable);
	}

	private void initQueueFromRoom(Board board, Box[][] grid, RoomType startingRoom, int diceRoll,
			Set<Integer> blockedBoxIds, Set<Box> reachable, Queue<Step> queue) {
		for (Box b : board.getBoxes()) {
			if (b.getType() == BoxType.DOOR && b.getRoom() == startingRoom) {
				for (int[] d : DELTAS) {
					int ox = b.getX() + d[0];
					int oy = b.getY() + d[1];
					if (isOutOfBounds(ox, oy)) {
						continue;
					}
					Box outside = grid[oy][ox];
					if (outside == null || blockedBoxIds.contains(outside.getId())
							|| outside.getRoom() == startingRoom) {
						continue;
					}

					if (outside.getType() == BoxType.DOOR && outside.getRoom() != null) {
						addRoomBoxes(board, outside.getRoom(), reachable);
						reachable.add(outside);
						if (diceRoll > 1) {
							enqueueStep(b.getId(), outside, diceRoll - 1, queue);
						}
					} else if (outside.getType() == BoxType.PATH || outside.getType() == BoxType.BONUS) {
						reachable.add(outside);
						if (diceRoll > 1) {
							enqueueStep(b.getId(), outside, diceRoll - 1, queue);
						}
					}
				}
			}
		}
	}

	private void processBFS(Board board, Box[][] grid, RoomType startingRoom, Set<Integer> blockedBoxIds,
			Set<Box> reachable, Queue<Step> queue) {
		while (!queue.isEmpty()) {
			Step current = queue.poll();
			Box currBox = current.box();
			int rem = current.remaining();

			if (rem <= 0) {
				continue;
			}

			for (int[] d : DELTAS) {
				int nx = currBox.getX() + d[0];
				int ny = currBox.getY() + d[1];

				if (isOutOfBounds(nx, ny) || BoardLayout.isUnclickable(nx, ny)) {
					continue;
				}

				Box neighbor = grid[ny][nx];
				if (neighbor == null || blockedBoxIds.contains(neighbor.getId())
						|| current.visitedPath().contains(neighbor.getId())) {
					continue;
				}

				// Caso 1: Vecino es DOOR de otra habitación
				if (neighbor.getType() == BoxType.DOOR && neighbor.getRoom() != null
						&& neighbor.getRoom() != startingRoom) {
					addRoomBoxes(board, neighbor.getRoom(), reachable);
					reachable.add(neighbor);
					if (rem - 1 > 0) {
						queue.add(nextStep(current, neighbor, rem - 1));
					}
					continue;
				}

				// Caso 2: Vecino es ROOM interior (las paredes no se atraviesan sin puerta)
				if (neighbor.getType() == BoxType.ROOM) {
					continue;
				}

				// Caso 3: Vecino es PATH o BONUS o DOOR de pasillo
				if (neighbor.getType() == BoxType.PATH || neighbor.getType() == BoxType.BONUS
						|| neighbor.getType() == BoxType.DOOR) {
					reachable.add(neighbor);
					if (rem - 1 > 0) {
						queue.add(nextStep(current, neighbor, rem - 1));
					}
				}
			}
		}
	}

	private Box[][] buildGridMap(Board board) {
		Box[][] grid = new Box[BoardLayout.HEIGHT][BoardLayout.WIDTH];
		for (Box b : board.getBoxes()) {
			grid[b.getY()][b.getX()] = b;
		}
		return grid;
	}

	private Set<Integer> findBlockedBoxIds(Player player) {
		Set<Integer> blockedBoxIds = new HashSet<>();
		if (player.getGame().getPlayers() == null) {
			return blockedBoxIds;
		}
		for (Player p : player.getGame().getPlayers()) {
			if (!p.getId().equals(player.getId()) && !p.isEliminated() && p.getCurrentBox() != null) {
				Box occupied = p.getCurrentBox();
				if (occupied.getType() != BoxType.ROOM) {
					blockedBoxIds.add(occupied.getId());
				}
			}
		}
		return blockedBoxIds;
	}

	private static boolean isOutOfBounds(int x, int y) {
		return x < 0 || x >= BoardLayout.WIDTH || y < 0 || y >= BoardLayout.HEIGHT;
	}

	private static void addRoomBoxes(Board board, RoomType room, Set<Box> reachable) {
		for (Box rBox : board.getBoxes()) {
			if (rBox.getRoom() == room) {
				reachable.add(rBox);
			}
		}
	}

	private static void enqueueStep(Integer originDoorId, Box outside, int remaining, Queue<Step> queue) {
		Set<Integer> initialPath = new HashSet<>();
		initialPath.add(originDoorId);
		initialPath.add(outside.getId());
		queue.add(new Step(outside, remaining, initialPath));
	}

	private static Step nextStep(Step current, Box nextBox, int remaining) {
		Set<Integer> nextPath = new HashSet<>(current.visitedPath());
		nextPath.add(nextBox.getId());
		return new Step(nextBox, remaining, nextPath);
	}
}
