package es.us.dp1.L5_5_26_27.cluedo.turn;

import static org.mockito.ArgumentMatchers.eq;
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

import es.us.dp1.L5_5_26_27.cluedo.configuration.SecurityConfiguration;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Turn Module")
@Feature("Turn Controller REST API")
@Owner("DP1-team")
@WebMvcTest(controllers = TurnRestController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
class TurnRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TurnService turnService;

    @Test
    @WithMockUser(username = "player1", authorities = { "PLAYER" })
    void shouldPerformInitialRollEndpoint() throws Exception {
        String code = "ABCDEF";
        InitialRollResult mockResult = new InitialRollResult(
                1,
                8,
                false,
                List.of(new PlayerRoll(1, 8, null)));

        when(turnService.initialRoll(eq(code))).thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/games/{code}/turns/initial-roll", code)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(1))
                .andExpect(jsonPath("$.diceResult").value(8))
                .andExpect(jsonPath("$.orderDetermined").value(false));
    }
}

