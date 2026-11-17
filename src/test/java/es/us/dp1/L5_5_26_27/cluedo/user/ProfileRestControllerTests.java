package es.us.dp1.L5_5_26_27.cluedo.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.L5_5_26_27.cluedo.configuration.SecurityConfiguration;
import es.us.dp1.L5_5_26_27.cluedo.configuration.jwt.JwtUtils;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.EmailAlreadyExistsException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.UsernameAlreadyExistsException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Users & Admin Module")
@Feature("Player profile")
@Owner("DP1-team")
@WebMvcTest(controllers = ProfileRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class ProfileRestControllerTests {

	private static final String BASE_URL = "/api/v1/profile";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtUtils jwtUtils;

	private User player;

	@BeforeEach
	void setup() {
		Authorities authority = new Authorities();
		authority.setId(2);
		authority.setAuthority("PLAYER");

		player = new User();
		player.setId(4);
		player.setUsername("player1");
		player.setPassword("secret");
		player.setEmail("player1@cluedo.test");
		player.setAvatar("/images/personajes/Rubio.png");
		player.setAccountStatus(AccountStatus.ACTIVE);
		player.setAuthority(authority);
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldReturnTheAuthenticatedProfile() throws Exception {
		when(userService.findCurrentUser()).thenReturn(player);

		mockMvc.perform(get(BASE_URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(4))
				.andExpect(jsonPath("$.username").value("player1"))
				.andExpect(jsonPath("$.email").value("player1@cluedo.test"))
				.andExpect(jsonPath("$.avatar").value("/images/personajes/Rubio.png"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.authority").doesNotExist())
				.andExpect(jsonPath("$.token").doesNotExist());
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldUpdateProfileAndRenewTheToken() throws Exception {
		User updated = new User();
		updated.setId(player.getId());
		updated.setUsername("player1");
		updated.setEmail("nuevo@cluedo.test");
		updated.setAvatar("/images/personajes/Amapola.png");
		updated.setPassword(player.getPassword());
		updated.setAuthority(player.getAuthority());
		updated.setAccountStatus(player.getAccountStatus());

		when(userService.updateCurrentProfile(any(ProfileUpdateRequest.class))).thenReturn(updated);
		when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("token-nuevo");

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("username", "player1");
		body.put("email", "nuevo@cluedo.test");
		body.put("avatar", "/images/personajes/Amapola.png");
		body.put("password", "otra");
		body.put("accountStatus", "BLOCKED");

		mockMvc.perform(put(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("player1"))
				.andExpect(jsonPath("$.email").value("nuevo@cluedo.test"))
				.andExpect(jsonPath("$.avatar").value("/images/personajes/Amapola.png"))
				.andExpect(jsonPath("$.token").value("token-nuevo"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.authority").doesNotExist())
				.andExpect(jsonPath("$.accountStatus").doesNotExist());

		ArgumentCaptor<ProfileUpdateRequest> captor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
		verify(userService).updateCurrentProfile(captor.capture());
		assertEquals("player1", captor.getValue().username());
		assertEquals("nuevo@cluedo.test", captor.getValue().email());
		assertEquals("/images/personajes/Amapola.png", captor.getValue().avatar());
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldRejectAUsernameThatAlreadyExists() throws Exception {
		when(userService.updateCurrentProfile(any(ProfileUpdateRequest.class)))
				.thenThrow(new UsernameAlreadyExistsException());

		mockMvc.perform(put(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new ProfileUpdateRequest("player2", "player1@cluedo.test", null))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Ese nombre de usuario ya está registrado"));

		verify(jwtUtils, never()).generateJwtToken(any(Authentication.class));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldRejectAnEmailThatAlreadyExists() throws Exception {
		when(userService.updateCurrentProfile(any(ProfileUpdateRequest.class)))
				.thenThrow(new EmailAlreadyExistsException());

		mockMvc.perform(put(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new ProfileUpdateRequest("player1", "player2@cluedo.test", null))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Ese correo electrónico ya está registrado"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldRejectAnInvalidProfile() throws Exception {
		mockMvc.perform(put(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new ProfileUpdateRequest("", "no-es-correo", null))))
				.andExpect(status().isBadRequest());

		verify(userService, never()).updateCurrentProfile(any());
	}

}
