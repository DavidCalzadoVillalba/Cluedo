package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class DiceAlreadyRolledException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public DiceAlreadyRolledException(String message) {
		super(message);
	}
}
