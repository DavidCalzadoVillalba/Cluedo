package es.us.dp1.L5_5_26_27.cluedo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Getter
public class ResourceNotOwnedException extends RuntimeException {

	private static final long serialVersionUID = -3906338266891937036L;

	public ResourceNotOwnedException(final Object object) {
		super(String.format("%s not owned.", object.getClass().getSimpleName()));
	}

}
