package es.us.dp1.L5_5_26_27.cluedo.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.auth.payload.request.SignupRequest;
import es.us.dp1.L5_5_26_27.cluedo.user.AuthoritiesService;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserService;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Users & Admin Module")
@Feature("Authentication")
@Owner("DP1-tutors")
@SpringBootTest
public class AuthServiceTests {

	@Autowired
	protected AuthService authService;
	@Autowired
	protected UserService userService;
	@Autowired
	protected AuthoritiesService authoritiesService;
	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Test
	@Transactional
	public void shouldCreateAdminUser() {
		SignupRequest request = createRequest("ADMIN", "admin2");
		int userFirstCount = ((Collection<User>) this.userService.findAll()).size();
		this.authService.createUser(request);
		int userLastCount = ((Collection<User>) this.userService.findAll()).size();
		assertEquals(userFirstCount + 1, userLastCount);
		User saved = this.userService.findUser("admin2");
		assertTrue(passwordEncoder.matches("prueba", saved.getPassword()));
	}



	@Test
	@Transactional
	public void shouldCreatePlayerUser() {
		SignupRequest request = createRequest("PLAYER", "playertest");
		int userFirstCount = ((Collection<User>) this.userService.findAll()).size();
		this.authService.createUser(request);
		int userLastCount = ((Collection<User>) this.userService.findAll()).size();
		assertEquals(userFirstCount + 1, userLastCount);
		User saved = this.userService.findUser("playertest");
		assertTrue(passwordEncoder.matches("prueba", saved.getPassword()));
	}

	private SignupRequest createRequest(String auth, String username) {
		SignupRequest request = new SignupRequest();
		request.setAuthority(auth);
		request.setPassword("prueba");
		request.setUsername(username);
		request.setEmail(username + "@cluedo.test");
		request.setAvatar("/images/personajes/Rubio.png");

		if(auth == "PLAYER") {
			User playerUser = new User();
			playerUser.setUsername("clinicOwnerTest");
			playerUser.setPassword("clinicOwnerTest");
			playerUser.setEmail("clinicownertest@cluedo.test");
			playerUser.setAuthority(authoritiesService.findByAuthority("PLAYER"));
			userService.saveUser(playerUser);
		}

		return request;
	}

}
