package es.us.dp1.L5_5_26_27.cluedo.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.L5_5_26_27.cluedo.configuration.jwt.JwtUtils;
import es.us.dp1.L5_5_26_27.cluedo.configuration.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/profile")
@SecurityRequirement(name = "bearerAuth")
class ProfileRestController {

	private final UserService userService;
	private final JwtUtils jwtUtils;

	@Autowired
	public ProfileRestController(UserService userService, JwtUtils jwtUtils) {
		this.userService = userService;
		this.jwtUtils = jwtUtils;
	}

	@GetMapping
	public ResponseEntity<ProfileResponse> getProfile() {
		return new ResponseEntity<>(ProfileResponse.of(userService.findCurrentUser()), HttpStatus.OK);
	}

	@PutMapping
	public ResponseEntity<ProfileResponse> updateProfile(@RequestBody @Valid ProfileUpdateRequest request) {
		User updated = userService.updateCurrentProfile(request);
		return new ResponseEntity<>(ProfileResponse.of(updated, refreshToken(updated)), HttpStatus.OK);
	}

	// The access token subject is the username, so a rename must renew it or the next request looks up the old name.
	private String refreshToken(User user) {
		UserDetailsImpl details = UserDetailsImpl.build(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(details, null,
				details.getAuthorities());
		return jwtUtils.generateJwtToken(authentication);
	}

}
