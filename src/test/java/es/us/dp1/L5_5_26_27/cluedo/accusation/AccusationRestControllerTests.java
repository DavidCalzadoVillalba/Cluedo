package es.us.dp1.L5_5_26_27.cluedo.accusation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import es.us.dp1.L5_5_26_27.cluedo.game.GameSolutionResponse;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Accusation Module")
@Feature("Accusation Controller REST API")
@Owner("DP1-team")
@WebMvcTest(controllers = AccusationRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class AccusationRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AccusationService accusationService;

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldAccuseSuccessfullyAndDelegateTheRequest() throws Exception {
		String code = "TEST01";
		AccusationRequest request = new AccusationRequest(CharacterType.AMAPOLA, WeaponType.PISTOLA, RoomType.KITCHEN);
		AccusationResponse response = new AccusationResponse(false, GameStatus.STARTED, null, List.of(),
				new GameSolutionResponse(CharacterType.RUBIO, WeaponType.CANDELABRO, RoomType.STUDY));

		when(accusationService.accuse(eq(code), any(AccusationRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/games/{code}/accusations", code)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correct").value(false))
				.andExpect(jsonPath("$.gameStatus").value("STARTED"))
				.andExpect(jsonPath("$.solution.character").value("RUBIO"))
				.andExpect(jsonPath("$.solution.weapon").value("CANDELABRO"))
				.andExpect(jsonPath("$.solution.room").value("STUDY"));

		verify(accusationService).accuse(eq(code), any(AccusationRequest.class));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldReturnTheWinnerWhenTheAccusationIsCorrect() throws Exception {
		String code = "TEST01";
		AccusationRequest request = new AccusationRequest(CharacterType.AMAPOLA, WeaponType.PISTOLA, RoomType.KITCHEN);
		Player winner = new Player();
		winner.setId(4);
		AccusationResponse response = new AccusationResponse(true, GameStatus.ENDED, winner, List.of(),
				new GameSolutionResponse(CharacterType.AMAPOLA, WeaponType.PISTOLA, RoomType.KITCHEN));

		when(accusationService.accuse(eq(code), any(AccusationRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/v1/games/{code}/accusations", code)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correct").value(true))
				.andExpect(jsonPath("$.gameStatus").value("ENDED"))
				.andExpect(jsonPath("$.winner.id").value(4))
				.andExpect(jsonPath("$.solution.character").value("AMAPOLA"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldRejectAnInvalidAccusationRequest() throws Exception {
		String invalidRequest = """
				{"character":null,"weapon":"PISTOLA","room":"KITCHEN"}
				""";

		mockMvc.perform(post("/api/v1/games/TEST01/accusations")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}
}
