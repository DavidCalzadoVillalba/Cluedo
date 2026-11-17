package es.us.dp1.L5_5_26_27.cluedo.exceptions;

public class GameRuleException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public GameRuleException(String message) {
		super(message);
	}

	public static final class InvalidGameNameException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public InvalidGameNameException() {
			super("El nombre de la sala debe tener entre 3 y 40 caracteres");
		}
	}

	public static final class InvalidPlayerCountException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public InvalidPlayerCountException() {
			super("El número de jugadores debe estar entre 2 y 6");
		}
	}

	public static final class DuplicateGameNameException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public DuplicateGameNameException() {
			super("Ya existe una partida activa con ese nombre");
		}
	}

	public static final class PlayerAlreadyInGameException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public PlayerAlreadyInGameException() {
			super("Ya estás en esta partida");
		}
	}

	public static final class GameFullException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public GameFullException() {
			super("La partida está completa");
		}
	}

	public static final class CharacterSelectionRequiredException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public CharacterSelectionRequiredException() {
			super("Debes elegir un personaje");
		}
	}

	public static final class CharacterAlreadySelectedException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public CharacterAlreadySelectedException() {
			super("Ese personaje ya ha sido elegido");
		}
	}

	public static final class HostCannotKickSelfException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public HostCannotKickSelfException() {
			super("El anfitrión no puede expulsarse a sí mismo");
		}
	}

	public static final class InsufficientPlayersException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public InsufficientPlayersException() {
			super("Se necesitan al menos 2 jugadores para iniciar la partida");
		}
	}

	public static final class GameNotWaitingException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public GameNotWaitingException() {
			super("La partida ya ha comenzado");
		}
	}

	public static final class InvalidGameCodeException extends GameRuleException {
		private static final long serialVersionUID = 1L;

		public InvalidGameCodeException() {
			super("El código de la partida es obligatorio");
		}
	}
}
