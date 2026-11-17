package es.us.dp1.L5_5_26_27.cluedo.notebook;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/games/{gameCode}/notebook/marks")
@SecurityRequirement(name = "bearerAuth")
public class NotebookRestController {

	private final NotebookService notebookService;

	public NotebookRestController(NotebookService notebookService) {
		this.notebookService = notebookService;
	}

	@GetMapping
	public ResponseEntity<List<NotebookMarkResponse>> getMarks(@PathVariable String gameCode) {
		return ResponseEntity.ok(notebookService.getMarks(gameCode));
	}

	@PutMapping("/{markId}")
	public ResponseEntity<NotebookMarkResponse> updateMark(
			@PathVariable String gameCode,
			@PathVariable Integer markId,
			@RequestBody @Valid NotebookMarkUpdateRequest request) {
		return ResponseEntity.ok(notebookService.updateMark(gameCode, markId, request.value()));
	}
}
