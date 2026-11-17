package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class GameNotStartedException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public GameNotStartedException(String message) {
		super(message);
	}
}
