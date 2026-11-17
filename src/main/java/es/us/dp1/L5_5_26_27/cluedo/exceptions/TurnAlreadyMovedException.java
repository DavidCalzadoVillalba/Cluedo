package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class TurnAlreadyMovedException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public TurnAlreadyMovedException() {
		super("Ya te has movido en este turno");
	}
}
