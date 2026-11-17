package es.us.dp1.L5_5_26_27.cluedo.turn;

import java.util.List;

public record InitialRollResult(Integer playerId, Integer diceResult, boolean orderDetermined, List<PlayerRoll> players) {
}
