package es.us.dp1.L5_5_26_27.cluedo.character;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import es.us.dp1.L5_5_26_27.cluedo.game.Game;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;

@Repository
public interface CharacterTokenRepository extends CrudRepository<CharacterToken, Integer> {

	List<CharacterToken> findByGame(Game game);

	List<CharacterToken> findByGameOrderByCharacterAsc(Game game);

	Optional<CharacterToken> findByGameAndCharacter(Game game, CharacterType character);

}
