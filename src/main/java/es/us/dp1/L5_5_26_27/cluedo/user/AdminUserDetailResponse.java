package es.us.dp1.L5_5_26_27.cluedo.user;

public record AdminUserDetailResponse(
		Integer id,
		String username,
		String email,
		String avatar,
		AccountStatus accountStatus,
		String authority,
		long finishedGames,
		long wins) {
}
