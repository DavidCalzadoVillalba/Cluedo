package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class NoActiveTurnException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public NoActiveTurnException() {
		super("No hay un turno activo");
	}

	public NoActiveTurnException(String message) {
		super(message);
	}
}
