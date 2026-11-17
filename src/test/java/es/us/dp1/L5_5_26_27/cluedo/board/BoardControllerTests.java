package es.us.dp1.L5_5_26_27.cluedo.board;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
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

import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.BoxType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.MoveRequest;
import es.us.dp1.L5_5_26_27.cluedo.configuration.SecurityConfiguration;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;

@WebMvcTest(controllers = BoardController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class BoardControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private BoardService boardService;

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	@DisplayName("GET /api/v1/games/{gameId}/board/reachable devuelve 200 con lista de ReachableBoxDto")
	void shouldGetReachableBoxes() throws Exception {
		Box box1 = new Box();
		box1.setId(50);
		box1.setX(10);
		box1.setY(5);
		box1.setType(BoxType.PATH);

		Box box2 = new Box();
		box2.setId(51);
		box2.setX(10);
		box2.setY(6);
		box2.setType(BoxType.DOOR);
		box2.setRoom(RoomType.KITCHEN);

		when(boardService.getReachableBoxes(eq("1"), eq(3))).thenReturn(List.of(box1, box2));

		mockMvc.perform(get("/api/v1/games/1/board/reachable")
				.param("diceRoll", "3")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(50))
				.andExpect(jsonPath("$[0].x").value(10))
				.andExpect(jsonPath("$[0].y").value(5))
				.andExpect(jsonPath("$[0].type").value("PATH"))
				.andExpect(jsonPath("$[1].id").value(51))
				.andExpect(jsonPath("$[1].room").value("KITCHEN"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	@DisplayName("POST /api/v1/games/{gameId}/board/move movimiento válido devuelve 200 con MoveResponse")
	void shouldMovePlayerSuccessfully() throws Exception {
		MoveRequest request = new MoveRequest(75, 4);

		Box movedBox = new Box();
		movedBox.setId(75);
		movedBox.setX(12);
		movedBox.setY(8);
		movedBox.setType(BoxType.PATH);

		when(boardService.movePlayer(eq("1"), eq(75), eq(4))).thenReturn(movedBox);

		mockMvc.perform(post("/api/v1/games/1/board/move")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.boxId").value(75))
				.andExpect(jsonPath("$.x").value(12))
				.andExpect(jsonPath("$.y").value(8))
				.andExpect(jsonPath("$.message").value("Movimiento realizado con éxito"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	@DisplayName("POST /api/v1/games/{gameId}/board/move movimiento ilegal devuelve 400 Bad Request")
	void shouldReturnBadRequestWhenMoveIsIllegal() throws Exception {
		MoveRequest request = new MoveRequest(999, 2);

		when(boardService.movePlayer(eq("1"), eq(999), eq(2)))
				.thenThrow(new IllegalArgumentException("Casilla no alcanzable con la tirada actual"));

		mockMvc.perform(post("/api/v1/games/1/board/move")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Casilla no alcanzable con la tirada actual"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	@DisplayName("GET /api/v1/games/{gameId}/board/reachable devuelve 403 Forbidden cuando el usuario no tiene acceso")
	void shouldReturnForbiddenWhenUserCannotAccessReachable() throws Exception {
		when(boardService.getReachableBoxes(eq("1"), eq(3)))
				.thenThrow(new AccessDeniedException("No es tu turno para moverte"));

		mockMvc.perform(get("/api/v1/games/1/board/reachable")
				.param("diceRoll", "3")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("No es tu turno para moverte"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	@DisplayName("GET /api/v1/games/{gameId}/board/reachable devuelve 404 Not Found cuando la partida no existe")
	void shouldReturnNotFoundWhenGameDoesNotExistForReachable() throws Exception {
		when(boardService.getReachableBoxes(eq("99"), eq(3)))
				.thenThrow(new ResourceNotFoundException("Partida", "id/código", "99"));

		mockMvc.perform(get("/api/v1/games/99/board/reachable")
				.param("diceRoll", "3")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	@DisplayName("POST /api/v1/games/{gameId}/board/move devuelve 403 Forbidden cuando el usuario no tiene permiso")
	void shouldReturnForbiddenWhenUserCannotMove() throws Exception {
		MoveRequest request = new MoveRequest(75, 4);

		when(boardService.movePlayer(eq("1"), eq(75), eq(4)))
				.thenThrow(new AccessDeniedException("El usuario autenticado no participa en esta partida"));

		mockMvc.perform(post("/api/v1/games/1/board/move")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("El usuario autenticado no participa en esta partida"));
	}
}
