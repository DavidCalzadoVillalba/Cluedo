package es.us.dp1.L5_5_26_27.cluedo.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import es.us.dp1.L5_5_26_27.cluedo.configuration.SecurityConfiguration;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Users & Admin Module")
@Feature("Community Directory")
@Owner("MMT1651")
@WebMvcTest(controllers = CommunityRestController.class,
	excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class),
	excludeAutoConfiguration = SecurityConfiguration.class)
class CommunityRestControllerTests {

	private static final String BASE_URL = "/api/v1/community/users";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	private PublicUserResponse user1;
	private PublicUserResponse user2;

	@BeforeEach
	void setup() {
		user1 = new PublicUserResponse(1, "DavidCalzado", "/static/images/avatars/scarlett.png");
		user2 = new PublicUserResponse(2, "LuciaGomez", null);
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldReturnPaginatedCommunityUsers() throws Exception {
		Page<PublicUserResponse> page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 6), 2);
		when(userService.findPublicUsers(eq(null), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get(BASE_URL)
				.param("page", "0")
				.param("size", "6"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.content[0].username").value("DavidCalzado"))
				.andExpect(jsonPath("$.content[0].avatar").value("/static/images/avatars/scarlett.png"))
				.andExpect(jsonPath("$.content[0].email").doesNotExist())
				.andExpect(jsonPath("$.content[0].password").doesNotExist())
				.andExpect(jsonPath("$.content[1].username").value("LuciaGomez"))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").value(1));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldFilterCommunityUsersBySearch() throws Exception {
		Page<PublicUserResponse> page = new PageImpl<>(List.of(user1), PageRequest.of(0, 6), 1);
		when(userService.findPublicUsers(eq("David"), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get(BASE_URL)
				.param("search", "David")
				.param("page", "0")
				.param("size", "6"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].username").value("DavidCalzado"));
	}

	@Test
	@WithMockUser(username = "player1", authorities = { "PLAYER" })
	void shouldAlsoWorkViaDirectoryAlias() throws Exception {
		Page<PublicUserResponse> page = new PageImpl<>(List.of(user2), PageRequest.of(0, 6), 1);
		when(userService.findPublicUsers(eq(null), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/v1/users/directory"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].username").value("LuciaGomez"));
	}

}
