package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class PlayerNotInGameException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public PlayerNotInGameException() {
		super("No perteneces a esta partida");
	}
}
