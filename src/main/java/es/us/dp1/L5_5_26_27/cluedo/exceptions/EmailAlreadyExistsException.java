package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class EmailAlreadyExistsException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public EmailAlreadyExistsException() {
		super("Ese correo electrónico ya está registrado");
	}

}
