package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class TurnExpiredException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public TurnExpiredException() {
		super("El tiempo del turno ha terminado");
	}
}
