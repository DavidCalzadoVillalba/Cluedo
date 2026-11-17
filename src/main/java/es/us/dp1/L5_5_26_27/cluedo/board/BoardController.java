package es.us.dp1.L5_5_26_27.cluedo.board;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.L5_5_26_27.cluedo.boxMovement.Box;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.MoveRequest;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.MoveResponse;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.ReachableBoxDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/games/{gameId}/board")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Board", description = "Endpoints de gestión de tablero y movimiento de jugadores")
public class BoardController {

	private final BoardService boardService;

	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	@GetMapping("/reachable")
	@Operation(summary = "Obtener casillas alcanzables según la tirada de dados para el jugador autenticado")
	public ResponseEntity<List<ReachableBoxDto>> getReachable(
			@PathVariable String gameId,
			@RequestParam int diceRoll) {
		List<Box> boxes = boardService.getReachableBoxes(gameId, diceRoll);
		List<ReachableBoxDto> dtoList = boxes.stream()
				.map(b -> new ReachableBoxDto(b.getId(), b.getX(), b.getY(), b.getType(), b.getRoom()))
				.toList();

		return ResponseEntity.ok(dtoList);
	}

	@PostMapping("/move")
	@Operation(summary = "Realizar movimiento a una casilla objetivo")
	public ResponseEntity<MoveResponse> move(
			@PathVariable String gameId,
			@RequestBody @Valid MoveRequest request) {
		Box movedBox = boardService.movePlayer(gameId, request.targetBoxId(), request.diceRoll());
		MoveResponse response = new MoveResponse(
				movedBox.getId(),
				movedBox.getX(),
				movedBox.getY(),
				movedBox.getType(),
				movedBox.getRoom(),
				"Movimiento realizado con éxito");

		return ResponseEntity.ok(response);
	}
}
