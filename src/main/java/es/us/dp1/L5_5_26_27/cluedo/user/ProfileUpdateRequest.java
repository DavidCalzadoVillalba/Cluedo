package es.us.dp1.L5_5_26_27.cluedo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
		@NotBlank(message = "El nombre de usuario es obligatorio")
		@Size(max = 255, message = "El nombre de usuario no puede superar los 255 caracteres")
		String username,

		@NotBlank(message = "El correo electrónico es obligatorio")
		@Email(message = "El correo electrónico no es válido")
		@Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
		String email,

		@Size(max = 255, message = "El avatar no puede superar los 255 caracteres")
		String avatar) {
}
