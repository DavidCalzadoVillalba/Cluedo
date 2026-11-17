package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class CardNotOwnedException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public CardNotOwnedException() {
		super("La carta no te pertenece");
	}
}
