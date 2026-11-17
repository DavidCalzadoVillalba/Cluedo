package es.us.dp1.L5_5_26_27.cluedo.user;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileResponse(Integer id, String username, String email, String avatar, String token) {

	public static ProfileResponse of(User user) {
		return of(user, null);
	}

	public static ProfileResponse of(User user, String token) {
		return new ProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAvatar(), token);
	}

}
