package es.us.dp1.L5_5_26_27.cluedo.auth;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import es.us.dp1.L5_5_26_27.cluedo.user.Authorities;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.us.dp1.L5_5_26_27.cluedo.auth.payload.request.SignupRequest;
import es.us.dp1.L5_5_26_27.cluedo.user.AccountStatus;
import es.us.dp1.L5_5_26_27.cluedo.user.AuthoritiesService;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;

@Service
public class AuthService {

	@SuppressWarnings("unused")
	private final PasswordEncoder encoder;
	private final AuthoritiesService authoritiesService;
	private final UserService userService;

	@Autowired
	public AuthService(PasswordEncoder encoder, AuthoritiesService authoritiesService, UserService userService) {
		this.encoder = encoder;
		this.authoritiesService = authoritiesService;
		this.userService = userService;
	}

	@Transactional
	public void createUser(@Valid SignupRequest request) {
		User user = new User();
		user.setUsername(request.getUsername());
		user.setPassword(request.getPassword());
		user.setEmail(resolveEmail(request));
		user.setAccountStatus(AccountStatus.ACTIVE);
		if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
			user.setAvatar(request.getAvatar().trim());
		}

		String strRoles = request.getAuthority() != null ? request.getAuthority().trim().toLowerCase() : "player";
		Authorities role;

		switch (strRoles) {
		case "admin":
			role = authoritiesService.findByAuthority("ADMIN");
			break;
		default:
			role = authoritiesService.findByAuthority("PLAYER");
		}
		user.setAuthority(role);
		userService.saveUser(user);
	}

	private String resolveEmail(SignupRequest request) {
		String email = request.getEmail();
		if (email == null || email.isBlank()) {
			return request.getUsername().toLowerCase() + "@cluedo.test";
		}
		return email;
	}

}
