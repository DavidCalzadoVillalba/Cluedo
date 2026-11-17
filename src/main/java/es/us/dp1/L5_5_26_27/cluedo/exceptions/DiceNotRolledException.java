package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class DiceNotRolledException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public DiceNotRolledException(String message) {
		super(message);
	}
}
