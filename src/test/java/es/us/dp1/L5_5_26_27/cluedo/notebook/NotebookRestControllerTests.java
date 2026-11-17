package es.us.dp1.L5_5_26_27.cluedo.notebook;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import es.us.dp1.L5_5_26_27.cluedo.configuration.SecurityConfiguration;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;

@WebMvcTest(
		controllers = NotebookRestController.class,
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class),
		excludeAutoConfiguration = SecurityConfiguration.class)
class NotebookRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private NotebookService notebookService;

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldReturnCurrentPlayersNotebookMarks() throws Exception {
		when(notebookService.getMarks("TEST01")).thenReturn(List.of(
				new NotebookMarkResponse(
						42, NotebookCategory.SUSPECT, CharacterType.PRADO, null, null, NotebookMarkValue.QUESTION)));

		mockMvc.perform(get("/api/v1/games/TEST01/notebook/marks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(42))
				.andExpect(jsonPath("$[0].category").value("SUSPECT"))
				.andExpect(jsonPath("$[0].character").value("PRADO"))
				.andExpect(jsonPath("$[0].value").value("QUESTION"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldUpdateNotebookMark() throws Exception {
		NotebookMarkResponse updated = new NotebookMarkResponse(
				42, NotebookCategory.SUSPECT, CharacterType.PRADO, null, null, NotebookMarkValue.CONFIRMED);
		when(notebookService.updateMark("TEST01", 42, NotebookMarkValue.CONFIRMED)).thenReturn(updated);

		mockMvc.perform(put("/api/v1/games/TEST01/notebook/marks/42")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new NotebookMarkUpdateRequest(NotebookMarkValue.CONFIRMED))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(42))
				.andExpect(jsonPath("$.value").value("CONFIRMED"));

		verify(notebookService).updateMark("TEST01", 42, NotebookMarkValue.CONFIRMED);
	}
}
