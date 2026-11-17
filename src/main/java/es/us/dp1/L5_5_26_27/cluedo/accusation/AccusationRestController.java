package es.us.dp1.L5_5_26_27.cluedo.accusation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/games")
@SecurityRequirement(name = "bearerAuth")
public class AccusationRestController {

	private final AccusationService accusationService;

	public AccusationRestController(AccusationService accusationService) {
		this.accusationService = accusationService;
	}

	@PostMapping("/{code}/accusations")
	public ResponseEntity<AccusationResponse> accuse(@PathVariable String code,
			@RequestBody @Valid AccusationRequest request) {
		return ResponseEntity.ok(accusationService.accuse(code, request));
	}
}
