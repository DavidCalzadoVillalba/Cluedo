package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class SecretPassageNotAllowedException extends GameRuleException {

	private static final long serialVersionUID = 1L;

	public SecretPassageNotAllowedException() {
		super("No puedes usar un pasadizo secreto si ya has tirado los dados");
	}
}
