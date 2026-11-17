package es.us.dp1.L5_5_26_27.cluedo.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping({"/api/v1/community/users", "/api/v1/users/directory"})
@SecurityRequirement(name = "bearerAuth")
public class CommunityRestController {

	private final UserService userService;

	@Autowired
	public CommunityRestController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<Page<PublicUserResponse>> getCommunityUsers(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String username,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) {
		int pageNumber = Math.max(page, 0);
		int pageSize = (size <= 0 || size > 50) ? 6 : size;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("username").ascending());
		String query = (search != null && !search.isBlank()) ? search : username;
		Page<PublicUserResponse> result = userService.findPublicUsers(query, pageable);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}
