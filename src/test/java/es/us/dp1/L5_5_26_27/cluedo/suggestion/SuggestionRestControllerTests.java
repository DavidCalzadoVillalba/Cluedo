package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.configuration.SecurityConfiguration;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Suggestion Module")
@Feature("Suggestion Controller REST API")
@Owner("DP1-team")
@WebMvcTest(controllers = SuggestionRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class SuggestionRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SuggestionService suggestionService;

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldSuggestSuccessfully() throws Exception {
		String code = "TEST01";
		SuggestionRequest request = new SuggestionRequest(CharacterType.AMAPOLA, WeaponType.PISTOLA, RoomType.KITCHEN);
		SuggestionResponse response = new SuggestionResponse(
				1,
				10,
				RoomType.KITCHEN,
				CharacterType.AMAPOLA,
				WeaponType.PISTOLA,
				SuggestionStatus.PENDING,
				2,
				"player2",
				1,
				"player1",
				null,
				null,
				null,
				false);

		when(suggestionService.suggest(eq(code), any(SuggestionRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/games/{code}/suggestions", code)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.room").value("KITCHEN"))
				.andExpect(jsonPath("$.character").value("AMAPOLA"))
				.andExpect(jsonPath("$.weapon").value("PISTOLA"))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.currentResponderId").value(2));
	}

	@Test
	@WithMockUser(username = "player2", authorities = { "PLAYER" })
	void shouldRefuteSuccessfully() throws Exception {
		String code = "TEST01";
		RefuteRequest request = new RefuteRequest(5);
		SuggestionResponse response = new SuggestionResponse(
				1,
				10,
				RoomType.KITCHEN,
				CharacterType.AMAPOLA,
				WeaponType.PISTOLA,
				SuggestionStatus.REFUTED,
				2,
				"player2",
				1,
				"player1",
				null,
				2,
				"player2",
				false);

		when(suggestionService.refute(eq(code), any(RefuteRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/games/{code}/suggestions/refute", code)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REFUTED"))
				.andExpect(jsonPath("$.refutedByPlayerName").value("player2"));
	}

	@Test
	@WithMockUser(username = "player2", authorities = { "PLAYER" })
	void shouldPassSuccessfully() throws Exception {
		String code = "TEST01";
		SuggestionResponse response = new SuggestionResponse(
				1,
				10,
				RoomType.KITCHEN,
				CharacterType.AMAPOLA,
				WeaponType.PISTOLA,
				SuggestionStatus.PENDING,
				3,
				"player3",
				1,
				"player1",
				null,
				null,
				null,
				false);

		when(suggestionService.pass(eq(code), eq(null))).thenReturn(response);

		mockMvc.perform(post("/api/v1/games/{code}/suggestions/pass", code)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.currentResponderId").value(3));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldGetCurrentSuggestion() throws Exception {
		String code = "TEST01";
		SuggestionResponse response = new SuggestionResponse(
				1,
				10,
				RoomType.KITCHEN,
				CharacterType.AMAPOLA,
				WeaponType.PISTOLA,
				SuggestionStatus.PENDING,
				2,
				"player2",
				1,
				"player1",
				null,
				null,
				null,
				false);

		when(suggestionService.getCurrentSuggestion(eq(code))).thenReturn(Optional.of(response));

		mockMvc.perform(get("/api/v1/games/{code}/suggestions/current", code)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.room").value("KITCHEN"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldReturnNoContentWhenNoCurrentSuggestion() throws Exception {
		String code = "TEST01";

		when(suggestionService.getCurrentSuggestion(eq(code))).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/v1/games/{code}/suggestions/current", code)
				.with(csrf()))
				.andExpect(status().isNoContent());
	}
}
