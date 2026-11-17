package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class SuggestionAlreadyResolvedException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public SuggestionAlreadyResolvedException() {
		super("La sugerencia ya ha sido resuelta");
	}
}
