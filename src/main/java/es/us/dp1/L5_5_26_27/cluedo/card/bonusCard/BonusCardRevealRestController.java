package es.us.dp1.L5_5_26_27.cluedo.card.bonusCard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.L5_5_26_27.cluedo.card.RevealCardRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/games")
@SecurityRequirement(name = "bearerAuth")
public class BonusCardRevealRestController {

	private final BonusCardRevealService bonusCardRevealService;

	public BonusCardRevealRestController(BonusCardRevealService bonusCardRevealService) {
		this.bonusCardRevealService = bonusCardRevealService;
	}

	@PostMapping("/{code}/bonus-card-reveals/{revealId}/reveal")
	public ResponseEntity<BonusCardRevealResponse> revealCard(
			@PathVariable String code,
			@PathVariable Integer revealId,
			@RequestBody @Valid RevealCardRequest request) {
		return ResponseEntity.ok(bonusCardRevealService.reveal(code, revealId, request.cardId()));
	}
}
