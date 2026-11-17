package es.us.dp1.L5_5_26_27.cluedo.configuration.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import es.us.dp1.L5_5_26_27.cluedo.user.Authorities;
import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTests {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

	private User sampleUser;

	@BeforeEach
	void setUp() {
		Authorities auth = new Authorities();
		auth.setId(1);
		auth.setAuthority("PLAYER");

		sampleUser = new User();
		sampleUser.setId(10);
		sampleUser.setUsername("detective1");
		sampleUser.setEmail("detective1@cluedo.test");
		sampleUser.setPassword("encodedPassword");
		sampleUser.setAuthority(auth);
	}

	@Test
	void shouldLoadUserByUsername() {
		when(userRepository.findByUsername("detective1")).thenReturn(Optional.of(sampleUser));

		UserDetails details = userDetailsService.loadUserByUsername("detective1");

		assertNotNull(details);
		assertEquals("detective1", details.getUsername());
		assertEquals("encodedPassword", details.getPassword());
	}

	@Test
	void shouldLoadUserByEmailWhenUsernameNotFound() {
		when(userRepository.findByUsername("detective1@cluedo.test")).thenReturn(Optional.empty());
		when(userRepository.findByEmailIgnoreCase("detective1@cluedo.test")).thenReturn(Optional.of(sampleUser));

		UserDetails details = userDetailsService.loadUserByUsername("detective1@cluedo.test");

		assertNotNull(details);
		assertEquals("detective1", details.getUsername());
	}

	@Test
	void shouldLoadUserByEmailCaseInsensitive() {
		when(userRepository.findByUsername("DETECTIVE1@CLUEDO.TEST")).thenReturn(Optional.empty());
		when(userRepository.findByEmailIgnoreCase("DETECTIVE1@CLUEDO.TEST")).thenReturn(Optional.of(sampleUser));

		UserDetails details = userDetailsService.loadUserByUsername("DETECTIVE1@CLUEDO.TEST");

		assertNotNull(details);
		assertEquals("detective1", details.getUsername());
	}

	@Test
	void shouldThrowUsernameNotFoundExceptionWhenNeitherUsernameNorEmailFound() {
		when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
		when(userRepository.findByEmailIgnoreCase("unknown")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> {
			userDetailsService.loadUserByUsername("unknown");
		});
	}

}
