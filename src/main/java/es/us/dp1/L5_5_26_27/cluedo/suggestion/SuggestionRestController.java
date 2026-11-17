package es.us.dp1.L5_5_26_27.cluedo.suggestion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/games")
@SecurityRequirement(name = "bearerAuth")
public class SuggestionRestController {

	private final SuggestionService suggestionService;

	public SuggestionRestController(SuggestionService suggestionService) {
		this.suggestionService = suggestionService;
	}

	@PostMapping("/{code}/suggestions")
	public ResponseEntity<SuggestionResponse> suggest(
			@PathVariable String code,
			@RequestBody @Valid SuggestionRequest request) {
		return ResponseEntity.ok(suggestionService.suggest(code, request));
	}

	@PostMapping("/{code}/suggestions/refute")
	public ResponseEntity<SuggestionResponse> refute(
			@PathVariable String code,
			@RequestBody @Valid RefuteRequest request) {
		return ResponseEntity.ok(suggestionService.refute(code, request));
	}

	@PostMapping("/{code}/suggestions/bonus-question")
	public ResponseEntity<SuggestionResponse> askExtraQuestion(
			@PathVariable String code,
			@RequestBody @Valid ExtraQuestionRequest request) {
		return ResponseEntity.ok(suggestionService.askExtraQuestion(code, request));
	}

	@PostMapping("/{code}/suggestions/peek")
	public ResponseEntity<SuggestionResponse> peekAtShownCard(
			@PathVariable String code,
			@RequestParam Integer bonusCardId) {
		return ResponseEntity.ok(suggestionService.peekAtShownCard(code, bonusCardId));
	}

	@PostMapping("/{code}/suggestions/pass")
	public ResponseEntity<SuggestionResponse> pass(
			@PathVariable String code,
			@RequestParam(required = false) Integer bonusCardId) {
		return ResponseEntity.ok(suggestionService.pass(code, bonusCardId));
	}

	@GetMapping("/{code}/suggestions/current")
	public ResponseEntity<SuggestionResponse> getCurrentSuggestion(@PathVariable String code) {
		return suggestionService.getCurrentSuggestion(code)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@GetMapping("/{code}/suggestions/neutral-cards")
	public ResponseEntity<java.util.List<NeutralCardResponse>> getNeutralCards(@PathVariable String code) {
		return ResponseEntity.ok(suggestionService.getNeutralCards(code));
	}

	@PostMapping("/{code}/suggestions/neutral-cards/{cardId}/consult")
	public ResponseEntity<NeutralCardResponse> consultNeutralCard(
			@PathVariable String code,
			@PathVariable Integer cardId) {
		return ResponseEntity.ok(suggestionService.consultNeutralCard(code, cardId));
	}
}
