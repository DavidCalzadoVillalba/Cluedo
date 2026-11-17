package es.us.dp1.L5_5_26_27.cluedo.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends CrudRepository<User, Integer>, PagingAndSortingRepository<User, Integer> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmailIgnoreCase(String email);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	boolean existsByUsernameAndIdNot(String username, Integer id);

	boolean existsByEmailAndIdNot(String email, Integer id);

	Optional<User> findById(Integer id);

	@Query("SELECT u FROM User u WHERE u.authority.authority = :auth")
	Iterable<User> findAllByAuthority(String auth);

	Page<User> findAll(Pageable pageable);

	Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

}
