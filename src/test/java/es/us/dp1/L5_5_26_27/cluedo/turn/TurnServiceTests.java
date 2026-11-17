package es.us.dp1.L5_5_26_27.cluedo.turn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStateResponse;
import es.us.dp1.L5_5_26_27.cluedo.game.GameService;
import es.us.dp1.L5_5_26_27.cluedo.game.GameStatus;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.BoxType;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.SecretPassageNotAllowedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.TurnAlreadyMovedException;
import es.us.dp1.L5_5_26_27.cluedo.board.BoardService;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCard;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardEffect;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardRepository;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusDeckRepository;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Turn Module")
@Feature("Initial Roll, Turn Order & Turn Timer")
@Owner("DP1-team")
@SpringBootTest
@AutoConfigureTestDatabase
class TurnServiceTests {

        @Autowired
        private TurnService turnService;

        @Autowired
        private GameService gameService;

        @Autowired
        private PlayerRepository playerRepository;

        @Autowired
        private TurnRepository turnRepository;

        @Autowired
        private BonusCardRepository bonusCardRepository;

        @Autowired
        private BonusDeckRepository bonusDeckRepository;

        @Autowired
        private BoardService boardService;

        private void asUser(String username) {
                var auth = new UsernamePasswordAuthenticationToken(
                                username, "0wn3r",
                                List.of(new SimpleGrantedAuthority("PLAYER")));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
        }

        private Game createAndStartGame(String roomName) {
                asUser("player1");
                Game game = gameService.createGame(roomName, 3, CharacterType.AMAPOLA);
                String code = game.getCode();

                asUser("player2");
                gameService.joinGame(code, CharacterType.RUBIO);

                asUser("player3");
                gameService.joinGame(code, CharacterType.BLANCO);

                asUser("player1");
                return gameService.startGame(code);
        }

        private Game createAndStartTwoPlayerGame(String roomName) {
                asUser("player1");
                Game game = gameService.createGame(roomName, 2, CharacterType.AMAPOLA);
                String code = game.getCode();

                asUser("player2");
                gameService.joinGame(code, CharacterType.RUBIO);

                asUser("player1");
                return gameService.startGame(code);
        }

        @Test
        @Transactional
        void shouldPerformInitialRoll() {
                Game game = createAndStartGame("SalaTestTirada1");
                String code = game.getCode();

                asUser("player1");
                InitialRollResult result = turnService.initialRoll(code);

                assertNotNull(result);
                assertNotNull(result.diceResult());
                assertTrue(result.diceResult() >= 2 && result.diceResult() <= 12);
                assertFalse(result.orderDetermined());

                Player p1 = playerRepository.findByGameAndUser(game, game.getHost()).orElseThrow();
                assertNotNull(p1.getInitialDiceOne());
                assertNotNull(p1.getInitialDiceTwo());
                assertEquals(result.diceResult(), p1.getInitialDiceResult());
        }

        @Test
        @Transactional
        void shouldFailWhenPlayerRollsTwice() {
                Game game = createAndStartGame("SalaTestTirada2");
                String code = game.getCode();

                asUser("player1");
                turnService.initialRoll(code);

                assertThrows(IllegalArgumentException.class, () -> turnService.initialRoll(code));
        }

        @Test
        @Transactional
        void shouldDetermineOrderAndStartFirstTurnWhenAllPlayersRoll() {
                Game game = createAndStartGame("SalaTestTirada3");
                String code = game.getCode();

                asUser("player1");
                InitialRollResult r1 = turnService.initialRoll(code);
                assertFalse(r1.orderDetermined());

                asUser("player2");
                InitialRollResult r2 = turnService.initialRoll(code);
                assertFalse(r2.orderDetermined());

                asUser("player3");
                InitialRollResult r3 = turnService.initialRoll(code);
                assertTrue(r3.orderDetermined());

                List<Player> players = playerRepository.findByGameOrderByIdAsc(game);
                for (Player p : players) {
                        assertNotNull(p.getInitialDiceResult());
                        assertNotNull(p.getTurnOrder());
                        assertTrue(p.getTurnOrder() >= 1 && p.getTurnOrder() <= 3);
                }

                // El turno activo debe estar creado para el jugador con turnOrder 1
                Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElse(null);
                assertNotNull(activeTurn);
                assertEquals(1, activeTurn.getTurnNumber());
                assertEquals(1, activeTurn.getPlayer().getTurnOrder());
                assertNotNull(activeTurn.getExpiresAt());
                assertTrue(activeTurn.getExpiresAt().isAfter(Instant.now()));
                assertTrue(activeTurn.getRemainingSeconds() > 0);
        }

        @Test
        @Transactional
        void shouldSkipEliminatedPlayerAndContinueWithTheRemainingPlayers() {
                Game game = createAndStartGame("TurnoTrasAcusacionFallida");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn activeTurn = turnRepository
                                .findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player eliminatedPlayer = activeTurn.getPlayer();
                eliminatedPlayer.setEliminated(true);
                playerRepository.save(eliminatedPlayer);

                asUser(eliminatedPlayer.getUser().getUsername());
                Turn nextTurn = turnService.finishTurn(activeTurn.getId());

                assertEquals(TurnStatus.FINISHED, activeTurn.getStatus());
                assertFalse(nextTurn.getPlayer().isEliminated());
                assertFalse(nextTurn.getPlayer().getId().equals(eliminatedPlayer.getId()));
                assertEquals(GameStatus.STARTED, game.getStatus());
        }

        @Test
        @Transactional
        void shouldEndGameWithoutWinnerAndExposeSolutionWhenEveryPlayerIsEliminated() {
                Game game = createAndStartGame("TodosEliminados");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn activeTurn = turnRepository
                                .findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                List<Player> players = playerRepository.findByGameOrderByIdAsc(game);
                players.forEach(player -> player.setEliminated(true));
                playerRepository.saveAll(players);

                asUser(activeTurn.getPlayer().getUser().getUsername());
                turnService.finishTurn(activeTurn.getId());

                GameStateResponse finalState = gameService.getGameState(game.getCode());

                assertEquals(GameStatus.ENDED, finalState.status());
                assertNull(finalState.winner());
                assertNull(finalState.activeTurn());
                assertNotNull(finalState.solution());
        }

        @Test
        @Transactional
        void shouldTimeoutAndAdvanceTurnWhenExpired() {
                Game game = createAndStartGame("SalaTestTimer1");
                String code = game.getCode();

                asUser("player1");
                turnService.initialRoll(code);
                asUser("player2");
                turnService.initialRoll(code);
                asUser("player3");
                turnService.initialRoll(code);

                Turn firstTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                assertEquals(TurnStatus.ACTIVE, firstTurn.getStatus());

                // Simulamos que el tiempo del turno expira
                firstTurn.setExpiresAt(Instant.now().minusSeconds(5));
                turnRepository.save(firstTurn);

                // El cronjob o servicio procesa turnos expirados
                turnService.finishExpiredTurns();

                Turn updatedFirstTurn = turnRepository.findById(firstTurn.getId()).orElseThrow();
                assertEquals(TurnStatus.TIMEOUT, updatedFirstTurn.getStatus());

                Turn nextTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                assertEquals(2, nextTurn.getTurnNumber());
                assertEquals(TurnStatus.ACTIVE, nextTurn.getStatus());
                assertNotNull(nextTurn.getExpiresAt());
                assertTrue(nextTurn.getExpiresAt().isAfter(Instant.now()));
        }

        @Test
        @Transactional
        void shouldFailActionWhenTurnIsExpired() {
                Game game = createAndStartGame("SalaTestTimer2");
                String code = game.getCode();

                asUser("player1");
                turnService.initialRoll(code);
                asUser("player2");
                turnService.initialRoll(code);
                asUser("player3");
                turnService.initialRoll(code);

                Turn activeTurn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();

                // Expira el turno
                activeTurn.setExpiresAt(Instant.now().minusSeconds(2));
                turnRepository.save(activeTurn);

                asUser(activeTurn.getPlayer().getUser().getUsername());
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                () -> turnService.roll(activeTurn.getId()));
                assertTrue(exception.getMessage().contains("terminado"));
        }

        @Test
        @Transactional
        void shouldApplyAndConsumeExtraDiceBonusCard() {
                Game game = createAndStartGame("BonusDadoExtra");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                asUser(turn.getPlayer().getUser().getUsername());
                var deck = bonusDeckRepository.findByGame(game).orElseThrow();
                BonusCard card = new BonusCard(BonusCardEffect.EXTRA_DICE_6, false, 99);
                card.setDeck(deck);
                card.setHolder(turn.getPlayer());
                card = bonusCardRepository.save(card);

                turnService.useBonusCard(turn.getId(), card.getId(), null);
                Turn rolled = turnService.roll(turn.getId());

                assertTrue(rolled.getDiceResult() >= 8 && rolled.getDiceResult() <= 18);
                BonusCard consumed = bonusCardRepository.findById(card.getId()).orElseThrow();
                assertTrue(consumed.isUsed());
                assertTrue(consumed.getHolder() == null);
        }

        @Test
        @Transactional
        void shouldGiveSixMoreMovementImmediatelyWhenExtraDiceIsPlayedAfterMoving() {
                Game game = createAndStartGame("BonusDadoTrasMovimiento");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player bonusPlayer = turn.getPlayer();
                asUser(bonusPlayer.getUser().getUsername());
                var deck = bonusDeckRepository.findByGame(game).orElseThrow();
                BonusCard card = new BonusCard(BonusCardEffect.EXTRA_DICE_6, false, 99);
                card.setDeck(deck);
                card.setHolder(bonusPlayer);
                card = bonusCardRepository.save(card);

                turn.setDiceResult(8);
                turn.setHasMoved(true);
                turnRepository.save(turn);
                turnService.useBonusCard(turn.getId(), card.getId(), null);

                Turn bonusMovement = turnRepository.findById(turn.getId()).orElseThrow();
                assertEquals(6, bonusMovement.getDiceResult());
                assertFalse(bonusMovement.isHasMoved());
                assertFalse(turnService.getReachableBoxes(bonusMovement.getId()).isEmpty());

                var destination = turnService.getReachableBoxes(bonusMovement.getId()).get(0);
                TurnActionResponse moveResult = turnService.move(bonusMovement.getId(), destination.getId());
                assertTrue(moveResult.turn().isHasMoved());

                BonusCard consumed = bonusCardRepository.findById(card.getId()).orElseThrow();
                assertTrue(consumed.isUsed());
                assertTrue(consumed.getHolder() == null);
        }

        @Test
        @Transactional
        void shouldDrawBonusCardWhenMovingToBonusBox() {
                Game game = createAndStartGame("MovimientoCasillaBonus");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                asUser(turn.getPlayer().getUser().getUsername());
                var deck = bonusDeckRepository.findByGame(game).orElseThrow();
                BonusCard nextCard = bonusCardRepository
                                .findByDeckAndHolderIsNullAndUsedFalseOrderByDeckOrderAsc(deck).get(0);
                nextCard.setEffect(BonusCardEffect.EXTRA_TURN);
                nextCard.setImmediate(true);
                bonusCardRepository.save(nextCard);
                var bonusBox = boardService.getReachableBoxes(turn.getPlayer(), 12).stream()
                                .filter(box -> box.getType() == BoxType.BONUS)
                                .findFirst()
                                .orElseThrow();
                turn.setDiceResult(12);
                turnRepository.save(turn);

                TurnActionResponse result = turnService.move(turn.getId(), bonusBox.getId());

                assertTrue(result.landedOnBonus());
                assertNotNull(result.drawnBonusCard());
                assertNotNull(result.drawnBonusCard().effect());
                assertEquals(BonusCardEffect.EXTRA_TURN, result.drawnBonusCard().effect());
                assertTrue(bonusCardRepository.findById(result.drawnBonusCard().id()).orElseThrow().getHolder()
                                .getId().equals(turn.getPlayer().getId()));
                assertFalse(turnRepository.findById(turn.getId()).orElseThrow().isExtraTurn());

                turnService.useBonusCard(turn.getId(), result.drawnBonusCard().id(), null);

                Turn nextTurn = turnService.finishTurn(turn.getId());
                assertEquals(turn.getPlayer().getId(), nextTurn.getPlayer().getId());
        }

        @Test
        @Transactional
        void shouldNotCreateBonusDeckOrDrawCardsInTwoPlayerGames() {
                Game game = createAndStartTwoPlayerGame("BonusDosJugadores");
                assertFalse(bonusDeckRepository.findByGame(game).isPresent());

                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                asUser(turn.getPlayer().getUser().getUsername());
                var bonusBox = boardService.getReachableBoxes(turn.getPlayer(), 12).stream()
                                .filter(box -> box.getType() == BoxType.BONUS)
                                .findFirst()
                                .orElseThrow();
                turn.setDiceResult(12);
                turnRepository.save(turn);

                TurnActionResponse result = turnService.move(turn.getId(), bonusBox.getId());
                GameStateResponse gameState = gameService.getGameState(game.getCode());

                assertTrue(result.landedOnBonus());
                assertNull(result.drawnBonusCard());
                assertTrue(gameState.myBonusCards().isEmpty());
                assertFalse(bonusDeckRepository.findByGame(game).isPresent());
        }

        @Test
        @Transactional
        void shouldAddStoredBonusCardToPlayerHandWhenMovingToBonusBox() {
                Game game = createAndStartGame("MovimientoBonusAlmacenable");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                asUser(turn.getPlayer().getUser().getUsername());
                var deck = bonusDeckRepository.findByGame(game).orElseThrow();
                BonusCard nextCard = bonusCardRepository
                                .findByDeckAndHolderIsNullAndUsedFalseOrderByDeckOrderAsc(deck).get(0);
                nextCard.setEffect(BonusCardEffect.EXTRA_DICE_6);
                nextCard.setImmediate(false);
                bonusCardRepository.save(nextCard);
                var bonusBox = boardService.getReachableBoxes(turn.getPlayer(), 12).stream()
                                .filter(box -> box.getType() == BoxType.BONUS)
                                .findFirst()
                                .orElseThrow();
                turn.setDiceResult(12);
                turnRepository.save(turn);

                TurnActionResponse result = turnService.move(turn.getId(), bonusBox.getId());
                GameStateResponse gameState = gameService.getGameState(game.getCode());

                assertNotNull(result.drawnBonusCard());
                assertEquals(BonusCardEffect.EXTRA_DICE_6, result.drawnBonusCard().effect());
                assertFalse(result.drawnBonusCard().immediate());
                assertTrue(gameState.myBonusCards().stream()
                                .anyMatch(card -> card.id().equals(result.drawnBonusCard().id())));
        }

        // -------------------------------------------------------------
        // Tests para HU-009: Uso de pasadizos secretos
        // -------------------------------------------------------------

        @Test
        @Transactional
        void shouldUseSecretPassageFromKitchenToGarageSuccessfully() {
                Game game = createAndStartGame("PasadizoCocinaGaraje");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player player = turn.getPlayer();

                // Colocar al jugador en la Cocina (17, 3)
                Box kitchenBox = boardService.findBox(game.getBoard(), 17, 3);
                player.setCurrentBox(kitchenBox);
                playerRepository.save(player);

                asUser(player.getUser().getUsername());
                Turn updatedTurn = turnService.useSecretPassage(turn.getId());

                assertTrue(updatedTurn.isHasMoved());

                Player updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();
                assertNotNull(updatedPlayer.getCurrentBox());
                assertEquals(2, updatedPlayer.getCurrentBox().getX());
                assertEquals(16, updatedPlayer.getCurrentBox().getY());
                assertEquals(RoomType.GARAGE, updatedPlayer.getCurrentBox().getRoom());
                assertEquals(RoomType.GARAGE, updatedPlayer.getLastSuggestionRoom());
        }

        @Test
        @Transactional
        void shouldUseSecretPassageFromBedroomToLoungeSuccessfully() {
                Game game = createAndStartGame("PasadizoDormitorioSalon");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player player = turn.getPlayer();

                // Colocar al jugador en el Dormitorio (2, 3)
                Box bedroomBox = boardService.findBox(game.getBoard(), 2, 3);
                player.setCurrentBox(bedroomBox);
                playerRepository.save(player);

                asUser(player.getUser().getUsername());
                Turn updatedTurn = turnService.useSecretPassage(turn.getId());

                assertTrue(updatedTurn.isHasMoved());

                Player updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();
                assertNotNull(updatedPlayer.getCurrentBox());
                assertEquals(17, updatedPlayer.getCurrentBox().getX());
                assertEquals(15, updatedPlayer.getCurrentBox().getY());
                assertEquals(RoomType.LOUNGE, updatedPlayer.getCurrentBox().getRoom());
                assertEquals(RoomType.LOUNGE, updatedPlayer.getLastSuggestionRoom());
        }

        @Test
        @Transactional
        void shouldFailSecretPassageWhenPlayerAlreadyMoved() {
                Game game = createAndStartGame("PasadizoYaMovido");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player player = turn.getPlayer();

                Box kitchenBox = boardService.findBox(game.getBoard(), 17, 3);
                player.setCurrentBox(kitchenBox);
                playerRepository.save(player);

                turn.setHasMoved(true);
                turnRepository.save(turn);

                asUser(player.getUser().getUsername());
                TurnAlreadyMovedException ex = assertThrows(TurnAlreadyMovedException.class,
                                () -> turnService.useSecretPassage(turn.getId()));
                assertTrue(ex.getMessage().contains("Ya te has movido en este turno"));
        }

        @Test
        @Transactional
        void shouldFailSecretPassageWhenPlayerAlreadyRolledDice() {
                Game game = createAndStartGame("PasadizoYaTiroDados");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player player = turn.getPlayer();

                Box kitchenBox = boardService.findBox(game.getBoard(), 17, 3);
                player.setCurrentBox(kitchenBox);
                playerRepository.save(player);

                turn.setDiceResult(7);
                turnRepository.save(turn);

                asUser(player.getUser().getUsername());
                SecretPassageNotAllowedException ex = assertThrows(SecretPassageNotAllowedException.class,
                                () -> turnService.useSecretPassage(turn.getId()));
                assertTrue(ex.getMessage().contains("No puedes usar un pasadizo secreto si ya has tirado los dados"));
        }

        @Test
        @Transactional
        void shouldFailSecretPassageWhenNotPlayersTurn() {
                Game game = createAndStartGame("PasadizoTurnoAjeno");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player player = turn.getPlayer();

                Box kitchenBox = boardService.findBox(game.getBoard(), 17, 3);
                player.setCurrentBox(kitchenBox);
                playerRepository.save(player);

                // Identificar un jugador que no sea el del turno activo
                String otherUser = player.getUser().getUsername().equals("player1") ? "player2" : "player1";
                asUser(otherUser);

                AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                                () -> turnService.useSecretPassage(turn.getId()));
                assertTrue(ex.getMessage().contains("No es tu turno"));
        }

        @Test
        @Transactional
        void shouldFailSecretPassageWhenNotInRoomWithSecretPassage() {
                Game game = createAndStartGame("PasadizoSalaSinPasadizo");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player player = turn.getPlayer();

                // Colocar al jugador en el Baño (8, 1), sala que no tiene pasadizo secreto
                Box bathroomBox = boardService.findBox(game.getBoard(), 8, 1);
                player.setCurrentBox(bathroomBox);
                playerRepository.save(player);

                asUser(player.getUser().getUsername());
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> turnService.useSecretPassage(turn.getId()));
                assertTrue(ex.getMessage().contains("no tiene pasadizo secreto"));
        }

        @Test
        @Transactional
        void shouldFailSecretPassageWhenInHallway() {
                Game game = createAndStartGame("PasadizoEnPasillo");
                asUser("player1");
                turnService.initialRoll(game.getCode());
                asUser("player2");
                turnService.initialRoll(game.getCode());
                asUser("player3");
                turnService.initialRoll(game.getCode());

                Turn turn = turnRepository.findFirstByGameAndStatusOrderByTurnNumberDesc(game, TurnStatus.ACTIVE)
                                .orElseThrow();
                Player player = turn.getPlayer();

                // Colocar al jugador en un pasillo (9, 7) sin habitación
                Box hallwayBox = boardService.findBox(game.getBoard(), 9, 7);
                player.setCurrentBox(hallwayBox);
                playerRepository.save(player);

                asUser(player.getUser().getUsername());
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> turnService.useSecretPassage(turn.getId()));
                assertTrue(ex.getMessage().contains("No estás en ninguna habitación"));
        }
}
