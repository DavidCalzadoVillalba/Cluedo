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

import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.exceptions.EmailAlreadyExistsException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.ResourceNotFoundException;
import es.us.dp1.L5_5_26_27.cluedo.exceptions.UsernameAlreadyExistsException;
import es.us.dp1.L5_5_26_27.cluedo.player.PlayerRepository;

@Service
public class UserService {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private PlayerRepository playerRepository;

	@Autowired
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PlayerRepository playerRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.playerRepository = playerRepository;

	}

	@Transactional
	public User saveUser(User user) throws DataAccessException {
		if (user.getUsername() == null || user.getUsername().isBlank()) {
			throw new IllegalArgumentException("El nombre de usuario es obligatorio");
		}
		if (user.getEmail() == null || user.getEmail().isBlank()) {
			throw new IllegalArgumentException("El correo electrónico es obligatorio");
		}
		String username = user.getUsername().trim();
		String email = user.getEmail().trim();
		if (userRepository.existsByUsername(username)) {
			throw new UsernameAlreadyExistsException();
		}
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException();
		}
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
		return user;
	}


	@Transactional(readOnly = true)
	public User findUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
	}

	@Transactional(readOnly = true)
	public User findUser(Integer id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
	}

	@Transactional(readOnly = true)
	public AdminUserDetailResponse findAdminUserDetails(Integer id) {
		User user = findUser(id);
		return new AdminUserDetailResponse(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getAvatar(),
				user.getAccountStatus(),
				user.getAuthority().getAuthority(),
				playerRepository.countFinishedGamesByUserId(id),
				playerRepository.countWinsByUserId(id));
	}

	@Transactional(readOnly = true)
	public User findCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new ResourceNotFoundException("Nobody authenticated!");
		else
			return userRepository.findByUsername(auth.getName())
					.orElseThrow(() -> new ResourceNotFoundException("User", "Username", auth.getName()));
	}

	public Boolean existsUser(String username) {
		return userRepository.existsByUsername(username);
	}

	public Boolean existsEmail(String email) {
		return email != null && userRepository.existsByEmail(email.trim());
	}

	@Transactional(readOnly = true)
	public Iterable<User> findAll() {
		return userRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Page<PublicUserResponse> findPublicUsers(String search, Pageable pageable) {
		Page<User> page;
		if (search != null && !search.isBlank()) {
			page = userRepository.findByUsernameContainingIgnoreCase(search.trim(), pageable);
		} else {
			page = userRepository.findAll(pageable);
		}
		return page.map(PublicUserResponse::of);
	}

	public Iterable<User> findAllByAuthority(String auth) {
		return userRepository.findAllByAuthority(auth);
	}

	@Transactional
	public User updateCurrentProfile(@Valid ProfileUpdateRequest request) {
		User current = findCurrentUser();
		String username = request.username().trim();
		String email = request.email().trim();
		String avatar = normalizeAvatar(request.avatar());

		if (userRepository.existsByUsernameAndIdNot(username, current.getId())) {
			throw new UsernameAlreadyExistsException();
		}
		if (userRepository.existsByEmailAndIdNot(email, current.getId())) {
			throw new EmailAlreadyExistsException();
		}

		current.setUsername(username);
		current.setEmail(email);
		current.setAvatar(avatar);
		return userRepository.save(current);
	}

	private String normalizeAvatar(String avatar) {
		if (avatar == null || avatar.isBlank()) {
			return null;
		}
		return avatar.trim();
	}

	@Transactional
	public User updateUser(@Valid User user, Integer idToUpdate) {
		if (user.getUsername() == null || user.getUsername().isBlank()) {
			throw new IllegalArgumentException("El nombre de usuario es obligatorio");
		}
		if (user.getEmail() == null || user.getEmail().isBlank()) {
			throw new IllegalArgumentException("El correo electrónico es obligatorio");
		}
		String username = user.getUsername().trim();
		String email = user.getEmail().trim();
		if (userRepository.existsByUsernameAndIdNot(username, idToUpdate)) {
			throw new UsernameAlreadyExistsException();
		}
		if (userRepository.existsByEmailAndIdNot(email, idToUpdate)) {
			throw new EmailAlreadyExistsException();
		}

		User toUpdate = findUser(idToUpdate);
		toUpdate.setUsername(username);
		toUpdate.setEmail(email);
		toUpdate.setAvatar(normalizeAvatar(user.getAvatar()));
		if (user.getAccountStatus() != null) {
			toUpdate.setAccountStatus(user.getAccountStatus());
		}
		if (user.getAuthority() != null) {
			toUpdate.setAuthority(user.getAuthority());
		}
		if (user.getPassword() != null && !user.getPassword().isBlank()) {
			toUpdate.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		userRepository.save(toUpdate);

		return toUpdate;
	}

	@Transactional
	public void deleteUser(Integer id) {
		User toDelete = findUser(id);
		this.userRepository.delete(toDelete);
	}

}
