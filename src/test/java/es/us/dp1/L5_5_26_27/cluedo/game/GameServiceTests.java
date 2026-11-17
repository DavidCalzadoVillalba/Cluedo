package es.us.dp1.L5_5_26_27.cluedo.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.card.GameCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardEffect;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRepository;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusDeckRepository;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardLayout;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.CharacterSelectionRequiredException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.GameFullException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.GameNotWaitingException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.HostCannotKickSelfException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InsufficientPlayersException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidGameNameException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.InvalidPlayerCountException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.GameRuleException.PlayerAlreadyInGameException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponToken;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Game Module")
@Feature("Game Management")
@Owner("BYP5449")
@SpringBootTest
@AutoConfigureTestDatabase
class GameServiceTests {

    @Autowired
    private GameService gameService;

    @Autowired
    private BonusCardRepository bonusCardRepository;

    @Autowired
    private BonusDeckRepository bonusDeckRepository;

    private void asUser(String username) {
        var auth = new UsernamePasswordAuthenticationToken(
                username, "0wn3r",
                List.of(new SimpleGrantedAuthority("PLAYER")));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldCreateGame() {
        Game game = gameService.createGame("TestPartida", 4, CharacterType.AMAPOLA);
        assertNotNull(game.getId());
        assertEquals("TestPartida", game.getName());
        assertEquals(GameStatus.WAITING, game.getStatus());
        assertEquals(1, game.getPlayers().size());
        assertEquals(CharacterType.AMAPOLA, game.getPlayers().get(0).getCharacter());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldNotCreateGameWithShortName() {
        assertThrows(InvalidGameNameException.class,
                () -> gameService.createGame("AB", 4, CharacterType.RUBIO));
    }

    @ParameterizedTest(name = "accepts the name length {0} and player limit {1}")
    @CsvSource({ "3,2", "40,6" })
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldAcceptGameCreationAtInclusiveBoundaries(int nameLength, int maxPlayers) {
        String name = "G".repeat(nameLength);

        Game game = gameService.createGame(name, maxPlayers, CharacterType.RUBIO);

        assertEquals(name, game.getName());
        assertEquals(maxPlayers, game.getMaxPlayers());
    }

    @ParameterizedTest(name = "rejects player limit {0}")
    @ValueSource(ints = { 1, 7 })
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldRejectPlayerCountsOutsideInclusiveBoundaries(int maxPlayers) {
        assertThrows(InvalidPlayerCountException.class,
                () -> gameService.createGame("PartidaLimite", maxPlayers, CharacterType.RUBIO));
    }

    @ParameterizedTest(name = "rejects a game name with length {0}")
    @ValueSource(ints = { 0, 2, 41 })
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldRejectGameNamesOutsideInclusiveBoundaries(int nameLength) {
        String name = "G".repeat(nameLength);

        assertThrows(InvalidGameNameException.class,
                () -> gameService.createGame(name, 4, CharacterType.RUBIO));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = { "  " })
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldRejectMissingOrBlankGameNames(String name) {
        assertThrows(InvalidGameNameException.class,
                () -> gameService.createGame(name, 4, CharacterType.RUBIO));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldRejectMissingPlayerLimit() {
        assertThrows(InvalidPlayerCountException.class,
                () -> gameService.createGame("PartidaLimite", null, CharacterType.RUBIO));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldNotCreateGameWithNullCharacter() {
        assertThrows(CharacterSelectionRequiredException.class,
                () -> gameService.createGame("TestPartida", 4, null));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldNotCreateGameWithInvalidMaxPlayers() {
        assertThrows(InvalidPlayerCountException.class,
                () -> gameService.createGame("TestPartida", 7, CharacterType.BLANCO));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldStartGameAndInitializeBoard() {
        Game game = gameService.createGame("PartidaInicio", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        asUser("player1");
        Game started = gameService.startGame(code);

        assertEquals(GameStatus.STARTED, started.getStatus());
        assertNotNull(started.getBoard());
        assertEquals(BoardLayout.WIDTH * BoardLayout.HEIGHT, started.getBoard().getBoxes().size());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldNotStartGameWithOnlyOnePlayer() {
        Game game = gameService.createGame("SoloPartida", 4, CharacterType.AMAPOLA);
        assertThrows(InsufficientPlayersException.class,
                () -> gameService.startGame(game.getCode()));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldNotStartGameIfNotHost() {
        Game game = gameService.createGame("PartidaHost", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        assertThrows(AccessDeniedException.class,
                () -> gameService.startGame(code));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void shouldNotStartGameIfAlreadyStarted() {
        Game game = gameService.createGame("PartidaRepetida", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        asUser("player1");
        gameService.startGame(code);

        assertThrows(GameNotWaitingException.class,
                () -> gameService.startGame(code));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_duringWaiting_returnsEmptyTokensAndCards() {
        Game game = gameService.createGame("EstadoEspera", 4, CharacterType.AMAPOLA);

        GameStateResponse state = gameService.getGameState(game.getCode());

        assertEquals(GameStatus.WAITING, state.status());
        assertTrue(state.weaponTokens().isEmpty());
        assertTrue(state.myHand().isEmpty());
        assertTrue(state.myBonusCards().isEmpty());
        assertTrue(state.faceUpCards().isEmpty());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_withTwoPlayers_returnsSixWeaponTokensAndSevenCardsEach() {
        Game game = gameService.createGame("DosJugadores", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        asUser("player1");
        gameService.startGame(code);

        GameStateResponse state = gameService.getGameState(code);

        assertEquals(GameStatus.STARTED, state.status());

        List<WeaponToken> tokens = state.weaponTokens();
        assertEquals(6, tokens.size());
        tokens.forEach(t -> assertNotNull(t.getCurrentBox()));

        assertEquals(7, state.myHand().size());
        assertTrue(state.faceUpCards().isEmpty());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_revealsSolutionOnlyAfterGameEnds() {
        Game game = gameService.createGame("SolucionFinal", 2, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        asUser("player1");
        Game started = gameService.startGame(code);
        GameSolutionResponse expectedSolution = GameSolutionResponse.from(started.getSolution());
        GameStateResponse activeState = gameService.getGameState(code);

        assertEquals(GameStatus.STARTED, activeState.status());
        assertTrue(activeState.solutionSaved());
        assertNull(activeState.solution());

        started.setWinner(started.getPlayers().get(0));
        started.setStatus(GameStatus.ENDED);

        GameStateResponse finishedState = gameService.getGameState(code);

        assertEquals(GameStatus.ENDED, finishedState.status());
        assertEquals(started.getWinner(), finishedState.winner());
        assertEquals(expectedSolution, finishedState.solution());
        assertNull(finishedState.activeTurn());

        asUser("player2");
        GameStateResponse otherPlayerState = gameService.getGameState(code);
        assertEquals(expectedSolution, otherPlayerState.solution());
        assertEquals(started.getWinner(), otherPlayerState.winner());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_eachPlayerReceivesOnlyTheirOwnHand() {
        Game game = gameService.createGame("PartidaManos", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        asUser("player1");
        gameService.startGame(code);

        GameStateResponse state1 = gameService.getGameState(code);

        asUser("player2");
        GameStateResponse state2 = gameService.getGameState(code);

        assertEquals(7, state1.myHand().size());
        assertEquals(7, state2.myHand().size());

        List<Integer> hand1Ids = state1.myHand().stream().map(GameCard::getId).toList();
        List<Integer> hand2Ids = state2.myHand().stream().map(GameCard::getId).toList();
        assertTrue(hand1Ids.stream().noneMatch(hand2Ids::contains));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_withThreePlayers_noFaceUpCards() {
        Game game = gameService.createGame("TresJugadores", 6, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);
        asUser("player3");
        gameService.joinGame(code, CharacterType.PRADO);

        asUser("player1");
        gameService.startGame(code);

        GameStateResponse state = gameService.getGameState(code);

        assertTrue(state.faceUpCards().isEmpty());
        assertEquals(6, state.myHand().size());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_returnsOnlyCurrentPlayersBonusCards() {
        Game game = gameService.createGame("CartasBonusPrivadas", 3, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);
        asUser("player3");
        gameService.joinGame(code, CharacterType.PRADO);

        asUser("player1");
        Game started = gameService.startGame(code);
        Player player1 = started.getPlayers().stream()
                .filter(player -> player.getUser().getUsername().equals("player1"))
                .findFirst()
                .orElseThrow();
        var deck = bonusDeckRepository.findByGame(started).orElseThrow();
        BonusCard bonusCard = new BonusCard(BonusCardEffect.EXTRA_DICE_6, false, 99);
        bonusCard.setDeck(deck);
        bonusCard.setHolder(player1);
        bonusCard = bonusCardRepository.save(bonusCard);

        GameStateResponse player1State = gameService.getGameState(code);
        assertEquals(1, player1State.myBonusCards().size());
        assertEquals(bonusCard.getId(), player1State.myBonusCards().get(0).id());

        asUser("player2");
        GameStateResponse player2State = gameService.getGameState(code);
        assertTrue(player2State.myBonusCards().isEmpty());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_withFourPlayers_returnsTwoFaceUpCards() {
        Game game = gameService.createGame("CuatroJugadores", 6, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);
        asUser("player3");
        gameService.joinGame(code, CharacterType.PRADO);
        asUser("player4");
        gameService.joinGame(code, CharacterType.CELESTE);

        asUser("player1");
        gameService.startGame(code);

        GameStateResponse state = gameService.getGameState(code);

        assertEquals(2, state.faceUpCards().size());
        assertEquals(4, state.myHand().size());
    }

    @Test
    @WithMockUser(username = "player2", password = "0wn3r")
    @Transactional
    void getGameState_withNonExistentCode_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> gameService.getGameState("NOEXIS"));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void getGameState_whenUserNotParticipant_throwsAccessDeniedException() {
        Game game = gameService.createGame("PartidaPrivada", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        assertThrows(AccessDeniedException.class,
                () -> gameService.getGameState(code));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void validateAccess_success() {
        Game game = gameService.createGame("PartidaValida", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        Game validatedGame = gameService.validateAccess(code);

        assertNotNull(validatedGame);
        assertEquals(code, validatedGame.getCode());
        assertEquals(GameStatus.WAITING, validatedGame.getStatus());
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void validateAccess_withNonExistentCode_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> gameService.validateAccess("NOEXISTE"));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void validateAccess_whenGameAlreadyStarted_throwsGameNotWaitingException() {
        Game game = gameService.createGame("PartidaIniciada", 2, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        asUser("player1");
        gameService.startGame(code);

        asUser("player3");
        GameNotWaitingException ex = assertThrows(GameNotWaitingException.class,
                () -> gameService.validateAccess(code));
        assertTrue(ex.getMessage().contains("La partida ya ha comenzado"));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void validateAccess_whenUserAlreadyInGame_throwsPlayerAlreadyInGameException() {
        Game game = gameService.createGame("PartidaPropia", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        PlayerAlreadyInGameException ex = assertThrows(PlayerAlreadyInGameException.class,
                () -> gameService.validateAccess(code));
        assertTrue(ex.getMessage().contains("Ya estás en esta partida"));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void validateAccess_whenGameIsFull_throwsGameFullException() {
        Game game = gameService.createGame("PartidaLlena", 2, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        gameService.joinGame(code, CharacterType.RUBIO);

        asUser("player3");
        GameFullException ex = assertThrows(GameFullException.class,
                () -> gameService.validateAccess(code));
        assertTrue(ex.getMessage().contains("La partida está completa"));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void kickPlayer_success() {
        Game game = gameService.createGame("PartidaKick", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        Game gameWithPlayer2 = gameService.joinGame(code, CharacterType.RUBIO);
        Player player2 = gameWithPlayer2.getPlayers().stream()
                .filter(p -> "player2".equals(p.getUser().getUsername()))
                .findFirst().orElseThrow();
        Integer player2Id = player2.getId();

        asUser("player1");
        Game afterKick = gameService.kickPlayer(code, player2Id);

        assertEquals(1, afterKick.getPlayers().size());
        assertTrue(afterKick.getPlayers().stream().noneMatch(p -> p.getId().equals(player2Id)));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void kickPlayer_whenNotHost_throwsAccessDeniedException() {
        Game game = gameService.createGame("PartidaKickNoHost", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        Game gameWithPlayer2 = gameService.joinGame(code, CharacterType.RUBIO);
        Player player2 = gameWithPlayer2.getPlayers().stream()
                .filter(p -> "player2".equals(p.getUser().getUsername()))
                .findFirst().orElseThrow();
        Integer player2Id = player2.getId();

        asUser("player3");
        gameService.joinGame(code, CharacterType.PRADO);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> gameService.kickPlayer(code, player2Id));
        assertTrue(ex.getMessage().contains("Solo el anfitrión puede expulsar jugadores"));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void kickPlayer_whenHostKicksThemselves_throwsHostCannotKickSelfException() {
        Game game = gameService.createGame("PartidaKickSelf", 4, CharacterType.AMAPOLA);
        String code = game.getCode();
        Player hostPlayer = game.getPlayers().get(0);

        asUser("player1");
        HostCannotKickSelfException ex = assertThrows(HostCannotKickSelfException.class,
                () -> gameService.kickPlayer(code, hostPlayer.getId()));
        assertTrue(ex.getMessage().contains("El anfitrión no puede expulsarse a sí mismo"));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void kickPlayer_withNonExistentPlayerId_throwsResourceNotFoundException() {
        Game game = gameService.createGame("PartidaKick404", 4, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player1");
        assertThrows(ResourceNotFoundException.class,
                () -> gameService.kickPlayer(code, 999999));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void kickPlayer_whenPlayerBelongsToAnotherGame_throwsResourceNotFoundException() {
        Game game1 = gameService.createGame("PartidaUno", 4, CharacterType.AMAPOLA);
        asUser("player2");
        Game game1Updated = gameService.joinGame(game1.getCode(), CharacterType.RUBIO);
        Player player2InGame1 = game1Updated.getPlayers().stream()
                .filter(p -> "player2".equals(p.getUser().getUsername()))
                .findFirst().orElseThrow();

        asUser("player3");
        Game game2 = gameService.createGame("PartidaDos", 4, CharacterType.BLANCO);

        asUser("player3");
        assertThrows(ResourceNotFoundException.class,
                () -> gameService.kickPlayer(game2.getCode(), player2InGame1.getId()));
    }

    @Test
    @WithMockUser(username = "player1", password = "0wn3r")
    @Transactional
    void kickPlayer_whenGameAlreadyStarted_throwsGameNotWaitingException() {
        Game game = gameService.createGame("PartidaKickStarted", 2, CharacterType.AMAPOLA);
        String code = game.getCode();

        asUser("player2");
        Game gameWithPlayer2 = gameService.joinGame(code, CharacterType.RUBIO);
        Player player2 = gameWithPlayer2.getPlayers().stream()
                .filter(p -> "player2".equals(p.getUser().getUsername()))
                .findFirst().orElseThrow();
        Integer player2Id = player2.getId();

        asUser("player1");
        gameService.startGame(code);

        GameNotWaitingException ex = assertThrows(GameNotWaitingException.class,
                () -> gameService.kickPlayer(code, player2Id));
        assertTrue(ex.getMessage().contains("La partida ya ha comenzado"));
    }
}
