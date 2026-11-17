package es.us.dp1.L5_5_26_27.cluedo.weapon;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;

public interface WeaponTokenRepository extends CrudRepository<WeaponToken, Integer> {

	List<WeaponToken> findByGameOrderByWeaponAsc(Game game);

	Optional<WeaponToken> findByGameAndWeapon(Game game, WeaponType weapon);
}
