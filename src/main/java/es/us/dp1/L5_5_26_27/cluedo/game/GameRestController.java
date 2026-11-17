package es.us.dp1.L5_5_26_27.cluedo.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.player.Player;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/games")
@SecurityRequirement(name = "bearerAuth")
class GameRestController {

	private final GameService gameService;

	@Autowired
	public GameRestController(GameService gameService) {
		this.gameService = gameService;
	}

	@PostMapping
	public ResponseEntity<Game> create(@RequestBody @Valid Game game, @RequestParam CharacterType character) {
		Game created = gameService.createGame(game.getName(), game.getMaxPlayers(), character);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	@GetMapping("/active")
	public ResponseEntity<java.util.List<ActiveGameResponse>> allGames() {
		return new ResponseEntity<>(gameService.getAllGamesForAdministration(), HttpStatus.OK);
	}

	@GetMapping("/history")
	public ResponseEntity<java.util.List<GameHistoryResponse>> history() {
		return new ResponseEntity<>(gameService.getCurrentUserHistory(), HttpStatus.OK);
	}

	@GetMapping("{code}/join")
	public ResponseEntity<Game> validateAccess(@PathVariable("code") String code) {
		return new ResponseEntity<>(gameService.validateAccess(code), HttpStatus.OK);
	}

	@PostMapping("{code}/players")
	public ResponseEntity<Game> join(@PathVariable("code") String code, @RequestBody @Valid Player player) {
		Game game = gameService.joinGame(code, player.getCharacter());
		return new ResponseEntity<>(game, HttpStatus.CREATED);
	}

	@GetMapping("{code}")
	public ResponseEntity<GameStateResponse> gameState(@PathVariable("code") String code) {
		return new ResponseEntity<>(gameService.getGameState(code), HttpStatus.OK);
	}

	@DeleteMapping("{code}/players/{playerId}")
	public ResponseEntity<Game> kick(@PathVariable("code") String code, @PathVariable("playerId") Integer playerId) {
		return new ResponseEntity<>(gameService.kickPlayer(code, playerId), HttpStatus.OK);
	}

	@PostMapping("{code}/start")
	public ResponseEntity<Game> start(@PathVariable("code") String code) {
		return new ResponseEntity<>(gameService.startGame(code), HttpStatus.OK);
	}

}
