package es.us.dp1.L5_5_26_27.cluedo.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import es.us.dp1.L5_5_26_27.cluedo.accusation.AccusationRequest;
import es.us.dp1.L5_5_26_27.cluedo.board.RoomType;
import es.us.dp1.L5_5_26_27.cluedo.boxMovement.MoveRequest;
import es.us.dp1.L5_5_26_27.cluedo.player.CharacterType;
import es.us.dp1.L5_5_26_27.cluedo.user.ProfileUpdateRequest;
import es.us.dp1.L5_5_26_27.cluedo.weapon.WeaponType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

class RequestValidationTests {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
		factory.afterPropertiesSet();
		validator = factory;
	}

	static Stream<Arguments> requiredAccusationFields() {
		return Stream.of(
				Arguments.of(new AccusationRequest(null, WeaponType.PISTOLA, RoomType.KITCHEN), "character"),
				Arguments.of(new AccusationRequest(CharacterType.AMAPOLA, null, RoomType.KITCHEN), "weapon"),
				Arguments.of(new AccusationRequest(CharacterType.AMAPOLA, WeaponType.PISTOLA, null), "room"));
	}

	@ParameterizedTest(name = "null accusation field {1} is rejected")
	@MethodSource("requiredAccusationFields")
	void shouldRejectEachMissingAccusationField(AccusationRequest request, String field) {
		Set<ConstraintViolation<AccusationRequest>> violations = validator.validate(request);

		assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals(field));
	}

	@ParameterizedTest(name = "dice roll {0} is rejected")
	@MethodSource("invalidDiceRolls")
	void shouldRejectDiceRollOutsideItsLowerBoundary(int diceRoll) {
		Set<ConstraintViolation<MoveRequest>> violations = validator.validate(new MoveRequest(1, diceRoll));

		assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("diceRoll"));
	}

	static Stream<Integer> invalidDiceRolls() {
		return Stream.of(0, -1);
	}

	@ParameterizedTest(name = "valid dice roll {0} is accepted")
	@MethodSource("validDiceRolls")
	void shouldAcceptDiceRollAtOrAboveItsLowerBoundary(int diceRoll) {
		assertThat(validator.validate(new MoveRequest(1, diceRoll))).isEmpty();
	}

	static Stream<Integer> validDiceRolls() {
		return Stream.of(1, 6);
	}

	static Stream<Arguments> invalidProfiles() {
		return Stream.of(
				Arguments.of(new ProfileUpdateRequest(null, "player1@cluedo.test", null), "username"),
				Arguments.of(new ProfileUpdateRequest("", "player1@cluedo.test", null), "username"),
				Arguments.of(new ProfileUpdateRequest(" ", "player1@cluedo.test", null), "username"),
				Arguments.of(new ProfileUpdateRequest("a".repeat(256), "player1@cluedo.test", null), "username"),
				Arguments.of(new ProfileUpdateRequest("player1", null, null), "email"),
				Arguments.of(new ProfileUpdateRequest("player1", "", null), "email"),
				Arguments.of(new ProfileUpdateRequest("player1", "no-es-correo", null), "email"),
				Arguments.of(new ProfileUpdateRequest("player1", emailOfLength(101), null), "email"),
				Arguments.of(new ProfileUpdateRequest("player1", "player1@cluedo.test", "a".repeat(256)), "avatar"));
	}

	@ParameterizedTest(name = "invalid profile field {1} is rejected")
	@MethodSource("invalidProfiles")
	void shouldRejectInvalidProfileFields(ProfileUpdateRequest request, String field) {
		Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

		assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals(field));
	}

	@ParameterizedTest(name = "profile at boundary {0} is accepted")
	@MethodSource("validProfiles")
	void shouldAcceptProfileAtItsBoundaries(String caseName, ProfileUpdateRequest request) {
		assertThat(validator.validate(request)).isEmpty();
	}

	static Stream<Arguments> validProfiles() {
		return Stream.of(
				Arguments.of("minimum username and empty avatar",
						new ProfileUpdateRequest("a", "a@b.es", "")),
				Arguments.of("maximum lengths",
						new ProfileUpdateRequest("a".repeat(255), emailOfLength(100), "a".repeat(255))),
				Arguments.of("avatar omitted",
						new ProfileUpdateRequest("player1", "player1@cluedo.test", null)));
	}

	private static String emailOfLength(int length) {
		String local = "a@";
		String suffix = ".es";
		int labelCharacters = length - local.length() - suffix.length() - 1;
		int firstLabel = Math.min(63, labelCharacters);
		int secondLabel = labelCharacters - firstLabel;
		return local + "b".repeat(firstLabel) + "." + "c".repeat(secondLabel) + suffix;
	}
}
