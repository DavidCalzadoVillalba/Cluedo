package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class AccusationOutsideCenterException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public AccusationOutsideCenterException() {
		super("Solo puedes acusar desde la casilla central");
	}
}
