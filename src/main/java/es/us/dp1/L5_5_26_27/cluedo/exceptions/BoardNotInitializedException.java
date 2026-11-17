package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class BoardNotInitializedException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public BoardNotInitializedException() {
		super("La partida no tiene tablero");
	}

	public BoardNotInitializedException(String message) {
		super(message);
	}
}
