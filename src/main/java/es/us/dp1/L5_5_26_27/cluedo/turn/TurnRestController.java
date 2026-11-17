package es.us.dp1.L5_5_26_27.cluedo.turn;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.card.bonusCard.BonusCardResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class TurnRestController {

	private final TurnService turnService;

	public TurnRestController(TurnService turnService) {
		this.turnService = turnService;
	}

	@PostMapping("/games/{code}/turns/initial-roll")
	public ResponseEntity<InitialRollResult> initialRoll(@PathVariable String code) {
		return new ResponseEntity<>(turnService.initialRoll(code), HttpStatus.OK);
	}

	@PostMapping("/turns/{id}/roll")
	public ResponseEntity<Turn> roll(@PathVariable Integer id) {
		return new ResponseEntity<>(turnService.roll(id), HttpStatus.OK);
	}

	@GetMapping("/turns/{id}/reachable-boxes")
	public ResponseEntity<List<Box>> getReachableBoxes(@PathVariable Integer id) {
		return new ResponseEntity<>(turnService.getReachableBoxes(id), HttpStatus.OK);
	}

	@PostMapping("/turns/{id}/move")
	public ResponseEntity<TurnActionResponse> move(@PathVariable Integer id, @RequestParam Integer boxId) {
		return new ResponseEntity<>(turnService.move(id, boxId), HttpStatus.OK);
	}

	@PostMapping("/turns/{id}/bonus-cards/{cardId}/use")
	public ResponseEntity<BonusCardResponse> useBonusCard(
			@PathVariable Integer id,
			@PathVariable Integer cardId,
			@RequestParam(required = false) RoomType room,
			@RequestParam(required = false) Integer targetPlayerId) {
		return new ResponseEntity<>(turnService.useBonusCard(id, cardId, room, targetPlayerId), HttpStatus.OK);
	}

	@PostMapping("/turns/{id}/secret-passage")
	public ResponseEntity<Turn> useSecretPassage(@PathVariable Integer id) {
		return new ResponseEntity<>(turnService.useSecretPassage(id), HttpStatus.OK);
	}

	@PostMapping("/turns/{id}/finish")
	public ResponseEntity<Turn> finish(@PathVariable Integer id) {
		return new ResponseEntity<>(turnService.finishTurn(id), HttpStatus.OK);
	}
}
