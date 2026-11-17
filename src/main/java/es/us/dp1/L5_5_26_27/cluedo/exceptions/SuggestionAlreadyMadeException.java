package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class SuggestionAlreadyMadeException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public SuggestionAlreadyMadeException() {
		super("Ya has formulado una sugerencia en este turno");
	}
}
