package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class UsernameAlreadyExistsException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public UsernameAlreadyExistsException() {
		super("Ese nombre de usuario ya está registrado");
	}

}
