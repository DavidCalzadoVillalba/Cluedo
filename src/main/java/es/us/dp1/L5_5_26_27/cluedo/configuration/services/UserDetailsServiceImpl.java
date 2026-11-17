package es.us.dp1.L5_5_26_27.cluedo.configuration.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.L5_5_26_27.cluedo.user.User;
import es.us.dp1.L5_5_26_27.cluedo.user.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		String identifier = usernameOrEmail != null ? usernameOrEmail.trim() : "";
		User user = userRepository.findByUsername(identifier)
				.or(() -> userRepository.findByEmailIgnoreCase(identifier))
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username or email: " + identifier));

		return UserDetailsImpl.build(user);
	}

}
