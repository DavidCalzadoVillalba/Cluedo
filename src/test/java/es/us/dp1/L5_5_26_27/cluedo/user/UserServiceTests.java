package es.us.dp1.L5_5_26_27.cluedo.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.exceptions.EmailAlreadyExistsException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.UsernameAlreadyExistsException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Users & Admin Module")
@Feature("Users Management")
@Owner("DP1-tutors")
@SpringBootTest
@AutoConfigureTestDatabase
class UserServiceTests {

	@Autowired
	private UserService userService;

	@Autowired
	private AuthoritiesService authService;


	@Test
	@WithMockUser(username = "player1", password = "0wn3r")
	void shouldFindCurrentUser() {
		User user = this.userService.findCurrentUser();
		assertEquals("player1", user.getUsername());
	}

	@Test
	@WithMockUser(username = "prueba")
	void shouldNotFindCorrectCurrentUser() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findCurrentUser());
	}

	@Test
	void shouldNotFindAuthenticated() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findCurrentUser());
	}

	@Test
	void shouldFindAllUsers() {
		List<User> users = (List<User>) this.userService.findAll();
		assertEquals(16, users.size());
	}

	@Test
	void shouldFindUsersByUsername() {
		User user = this.userService.findUser("player1");
		assertEquals("player1", user.getUsername());
	}

	@Test
	void shouldFindUsersByAuthority() {
		List<User> owners = (List<User>) this.userService.findAllByAuthority("PLAYER");
		assertEquals(15, owners.size());

		List<User> admins = (List<User>) this.userService.findAllByAuthority("ADMIN");
		assertEquals(1, admins.size());

		List<User> vets = (List<User>) this.userService.findAllByAuthority("VET");
		assertEquals(0, vets.size());
	}

	@Test
	void shouldNotFindUserByIncorrectUsername() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findUser("usernotexists"));
	}

	@Test
	void shouldFindSingleUser() {
		User user = this.userService.findUser(4);
		assertEquals("player1", user.getUsername());
	}

	@Test
	void shouldNotFindSingleUserWithBadID() {
		assertThrows(ResourceNotFoundException.class, () -> this.userService.findUser(100));
	}

	@Test
	void shouldExistUser() {
		assertEquals(true, this.userService.existsUser("player1"));
	}

	@Test
	void shouldNotExistUser() {
		assertEquals(false, this.userService.existsUser("player10000"));
	}

	@Test
	@Transactional
	void shouldUpdateUser() {
		int idToUpdate = 1;
		String newName="Change";
		User user = this.userService.findUser(idToUpdate);
		user.setUsername(newName);
		userService.updateUser(user, idToUpdate);
		user = this.userService.findUser(idToUpdate);
		assertEquals(newName, user.getUsername());
	}

	@Test
	@Transactional
	void shouldInsertUser() {
		int count = ((Collection<User>) this.userService.findAll()).size();

		User user = new User();
		user.setUsername("Sam");
		user.setPassword("password");
		user.setEmail("sam@cluedo.test");
		user.setAuthority(authService.findByAuthority("ADMIN"));

		this.userService.saveUser(user);
		assertNotEquals(0, user.getId().longValue());
		assertNotNull(user.getId());
		assertEquals("sam@cluedo.test", user.getEmail());
		assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());

		int finalCount = ((Collection<User>) this.userService.findAll()).size();
		assertEquals(count + 1, finalCount);
	}

	@Test
	@Transactional
	@WithMockUser(username = "player1")
	void shouldUpdateOnlyUsernameEmailAndAvatar() {
		User before = this.userService.findUser("player1");
		String password = before.getPassword();

		User updated = this.userService.updateCurrentProfile(new ProfileUpdateRequest("player1-edit",
				"player1-edit@cluedo.test", "/images/personajes/Amapola.png"));

		assertEquals(before.getId(), updated.getId());
		assertEquals("player1-edit", updated.getUsername());
		assertEquals("player1-edit@cluedo.test", updated.getEmail());
		assertEquals("/images/personajes/Amapola.png", updated.getAvatar());
		assertEquals(password, updated.getPassword());
		assertEquals(before.getAuthority().getAuthority(), updated.getAuthority().getAuthority());
		assertEquals(before.getAccountStatus(), updated.getAccountStatus());
	}

	@Test
	@Transactional
	@WithMockUser(username = "player1")
	void shouldAllowKeepingTheCurrentUsernameAndEmail() {
		User updated = this.userService.updateCurrentProfile(
				new ProfileUpdateRequest("player1", "player1@cluedo.test", "/images/personajes/Rubio.png"));

		assertEquals("player1", updated.getUsername());
		assertEquals("player1@cluedo.test", updated.getEmail());
		assertEquals("/images/personajes/Rubio.png", updated.getAvatar());
	}

	@Test
	@Transactional
	@WithMockUser(username = "player1")
	void shouldClearABlankAvatar() {
		User updated = this.userService.updateCurrentProfile(
				new ProfileUpdateRequest("  player1  ", "  player1@cluedo.test  ", "   "));

		assertEquals("player1", updated.getUsername());
		assertEquals("player1@cluedo.test", updated.getEmail());
		assertEquals(null, updated.getAvatar());
	}

	@Test
	@WithMockUser(username = "player1")
	void shouldRejectUsernameOwnedByAnotherUser() {
		UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class,
				() -> this.userService.updateCurrentProfile(
						new ProfileUpdateRequest("player2", "player1-otro@cluedo.test", null)));

		assertEquals("Ese nombre de usuario ya está registrado", exception.getMessage());
		assertEquals("player1", this.userService.findUser(4).getUsername());
	}

	@Test
	@WithMockUser(username = "player1")
	void shouldRejectEmailOwnedByAnotherUser() {
		EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
				() -> this.userService.updateCurrentProfile(
						new ProfileUpdateRequest("player1-otro", "player2@cluedo.test", null)));

		assertEquals("Ese correo electrónico ya está registrado", exception.getMessage());
		assertEquals("player1@cluedo.test", this.userService.findUser(4).getEmail());
	}

	@Test
	void shouldFindPublicUsersPaginated() {
		Page<PublicUserResponse> page = this.userService.findPublicUsers(null, PageRequest.of(0, 5));
		assertNotNull(page);
		assertEquals(5, page.getContent().size());
		assertTrue(page.getTotalElements() >= 10);
		PublicUserResponse first = page.getContent().get(0);
		assertNotNull(first.username());
	}

	@Test
	void shouldFindPublicUsersFilteredBySearch() {
		Page<PublicUserResponse> page = this.userService.findPublicUsers("player1", PageRequest.of(0, 10));
		assertNotNull(page);
		assertTrue(page.getContent().size() >= 2); // player1, player10
		assertTrue(page.getContent().stream().allMatch(u -> u.username().toLowerCase().contains("player1")));
	}

	@Test
	void shouldReturnEmptyPageWhenSearchMatchesNoUsers() {
		Page<PublicUserResponse> page = this.userService.findPublicUsers("nonexistent_user_xyz", PageRequest.of(0, 10));
		assertNotNull(page);
		assertEquals(0, page.getContent().size());
		assertEquals(0, page.getTotalElements());
	}

	@Test
	@Transactional
	void shouldUpdateUserFieldsAsAdmin() {
		int idToUpdate = 5; // player2
		User user = this.userService.findUser(idToUpdate);
		user.setUsername("player2-modified");
		user.setEmail("player2-modified@cluedo.test");
		user.setAccountStatus(AccountStatus.SUSPENDED);
		user.setAuthority(authService.findByAuthority("ADMIN"));
		user.setAvatar("https://example.com/new-avatar.png");
		user.setPassword("newsecret123");

		userService.updateUser(user, idToUpdate);

		User updated = this.userService.findUser(idToUpdate);
		assertEquals("player2-modified", updated.getUsername());
		assertEquals("player2-modified@cluedo.test", updated.getEmail());
		assertEquals(AccountStatus.SUSPENDED, updated.getAccountStatus());
		assertEquals("ADMIN", updated.getAuthority().getAuthority());
		assertEquals("https://example.com/new-avatar.png", updated.getAvatar());
	}

	@Test
	@Transactional
	void shouldRejectUpdateUserWithDuplicateUsername() {
		int idToUpdate = 5; // player2
		User payload = new User();
		payload.setUsername("player1"); // already owned by id 4
		payload.setEmail("player2@cluedo.test");

		assertThrows(UsernameAlreadyExistsException.class, () -> userService.updateUser(payload, idToUpdate));
	}

	@Test
	@Transactional
	void shouldRejectUpdateUserWithDuplicateEmail() {
		int idToUpdate = 5; // player2
		User payload = new User();
		payload.setUsername("player2");
		payload.setEmail("player1@cluedo.test"); // already owned by id 4

		assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(payload, idToUpdate));
	}

}
