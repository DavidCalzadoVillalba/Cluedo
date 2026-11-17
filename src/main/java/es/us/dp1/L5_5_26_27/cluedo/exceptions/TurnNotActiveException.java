package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class TurnNotActiveException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public TurnNotActiveException() {
		super("El turno no está activo");
	}
}
