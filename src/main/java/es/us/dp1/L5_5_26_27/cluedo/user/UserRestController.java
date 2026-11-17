/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.us.dp1.L5_5_26_27.cluedo.user;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.us.dp1.L5_5_26_27.cluedo.exceptions.AccessDeniedException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.util.RestPreconditions;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.L5_5_26_27.cluedo.auth.payload.response.MessageResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
class UserRestController {

	private final UserService userService;
	private final AuthoritiesService authService;

	@Autowired
	public UserRestController(UserService userService, AuthoritiesService authService) {
		this.userService = userService;
		this.authService = authService;
	}

	@GetMapping
	public ResponseEntity<List<User>> findAll(@RequestParam(required = false) String auth) {
		List<User> res;
		if (auth != null) {
			res = (List<User>) userService.findAllByAuthority(auth);
		} else
			res = (List<User>) userService.findAll();
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@GetMapping("authorities")
	public ResponseEntity<List<Authorities>> findAllAuths() {
		List<Authorities> res = (List<Authorities>) authService.findAll();
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@GetMapping(value = "{id}")
	public ResponseEntity<User> findById(@PathVariable("id") Integer id) {
		return new ResponseEntity<>(userService.findUser(id), HttpStatus.OK);
	}

	@GetMapping(value = "{id}/details")
	public ResponseEntity<AdminUserDetailResponse> findAdminDetails(@PathVariable("id") Integer id) {
		return new ResponseEntity<>(userService.findAdminUserDetails(id), HttpStatus.OK);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<User> create(@RequestBody @Valid User user) {
		if (user.getPassword() == null || user.getPassword().isBlank()) {
			throw new IllegalArgumentException("La contraseña es obligatoria");
		}
		if (user.getAuthority() == null || user.getAuthority().getAuthority() == null) {
			throw new IllegalArgumentException("El rol es obligatorio");
		}
		user.setAuthority(authService.findByAuthority(user.getAuthority().getAuthority().trim().toUpperCase()));
		User savedUser = userService.saveUser(user);
		return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
	}

	@PutMapping(value = "{userId}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<User> update(@PathVariable("userId") Integer id, @RequestBody @Valid User user) {
		RestPreconditions.checkNotNull(userService.findUser(id), "User", "ID", id);
		if (user.getAuthority() != null && user.getAuthority().getAuthority() != null) {
			Authorities foundAuth = authService.findByAuthority(user.getAuthority().getAuthority().trim().toUpperCase());
			if (foundAuth != null) {
				user.setAuthority(foundAuth);
			}
		}
		try {
			User currentUser = userService.findCurrentUser();
			if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(id)) {
				if (user.getAuthority() != null && !"ADMIN".equalsIgnoreCase(user.getAuthority().getAuthority())) {
					throw new AccessDeniedException("No puedes quitarte el rol de Administrador a ti mismo.");
				}
				if (user.getAccountStatus() != null && user.getAccountStatus() != AccountStatus.ACTIVE) {
					throw new AccessDeniedException("No puedes suspender o bloquear tu propia cuenta.");
				}
			}
		} catch (ResourceNotFoundException ignored) {
			// In case no authenticated user or currentUser cannot be resolved
		}
		return new ResponseEntity<>(this.userService.updateUser(user, id), HttpStatus.OK);
	}

	@DeleteMapping(value = "{userId}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<MessageResponse> delete(@PathVariable("userId") int id) {
		RestPreconditions.checkNotNull(userService.findUser(id), "User", "ID", id);
		if (userService.findCurrentUser().getId() != id) {
			userService.deleteUser(id);
			return new ResponseEntity<>(new MessageResponse("User deleted!"), HttpStatus.OK);
		} else
			throw new AccessDeniedException("You can't delete yourself!");
	}

}
