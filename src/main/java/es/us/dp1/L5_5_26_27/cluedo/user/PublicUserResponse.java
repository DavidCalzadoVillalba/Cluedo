package es.us.dp1.L5_5_26_27.cluedo.user;

public record PublicUserResponse(Integer id, String username, String avatar) {

	public static PublicUserResponse of(User user) {
		return new PublicUserResponse(user.getId(), user.getUsername(), user.getAvatar());
	}

}
