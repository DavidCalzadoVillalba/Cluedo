import {
    CHARACTER_IMAGES,
    CHARACTER_NAMES,
    ROOM_IMAGES,
    ROOM_NAMES,
    WEAPON_IMAGES,
    WEAPON_NAMES,
} from './gameConstants';

const SOLUTION_CARDS = [
    { key: 'character', label: 'Sospechoso', names: CHARACTER_NAMES, images: CHARACTER_IMAGES },
    { key: 'weapon', label: 'Arma', names: WEAPON_NAMES, images: WEAPON_IMAGES },
    { key: 'room', label: 'Habitación', names: ROOM_NAMES, images: ROOM_IMAGES },
];

export default function AccusationResultModal({
    mode,
    playerName,
    playerCharacter,
    solution,
    onClose,
    onReturnToMenu,
}) {
    if (!mode) return null;

    const victory = mode === 'victory';
    const spectator = mode === 'spectator-winner';
    const noWinner = mode === 'no-winner';
    const completeSolution = solution && SOLUTION_CARDS.every(({ key }) => solution[key]);
    const title = victory ? '¡Victoria!' : noWinner ? 'Derrota' : spectator ? 'Partida finalizada' : 'Derrota';

    return (
        <div className="accusation-result-backdrop">
            <section
                className={`accusation-result-modal ${victory ? 'is-victory' : 'is-defeat'}`}
                role="dialog"
                aria-modal="true"
                aria-labelledby="accusation-result-title"
            >
                <p className="accusation-result-eyebrow">
                    {noWinner ? 'Partida finalizada' : 'Misterio resuelto'}
                </p>
                <h1 id="accusation-result-title">{title}</h1>
                <p className="accusation-result-subtitle">
                    {victory || spectator
                        ? 'El misterio ha sido resuelto'
                        : noWinner ? 'Nadie ha resuelto el misterio' : 'Tu acusación final era incorrecta'}
                </p>

                {completeSolution ? (
                    <div className="accusation-result-cards" aria-label="Solución correcta">
                        {SOLUTION_CARDS.map(({ key, label, names, images }) => {
                            const value = solution[key];
                            const image = images[value];
                            return (
                                <article className="accusation-result-card" key={key}>
                                    {image && <img src={image} alt="" />}
                                    <span className="accusation-result-card-label">{label}</span>
                                    <strong>{names[value] || value}</strong>
                                </article>
                            );
                        })}
                    </div>
                ) : (
                    <p className="accusation-result-missing">No se ha podido cargar la solución de la partida.</p>
                )}

                <p className="accusation-result-message">
                    {victory ? (
                        <>
                            <strong>{playerName || 'Un jugador'}</strong>
                            {playerCharacter ? ` (${playerCharacter})` : ''} ha ganado la partida al resolver el misterio.
                        </>
                    ) : spectator ? (
                        <>
                            <strong>{playerName || 'Un jugador'}</strong>
                            {playerCharacter ? ` (${playerCharacter})` : ''} ha ganado la partida. Tú estabas viendo la partida.
                        </>
                    ) : noWinner ? (
                        <>
                            Todos los jugadores han fallado su acusación final. <strong>No ha ganado nadie.</strong>
                            {' '}La solución correcta era:
                        </>
                    ) : (
                        <>
                            <strong>{playerName || 'Tu personaje'}</strong>
                            {playerCharacter ? ` (${playerCharacter})` : ''} ha fallado la acusación y queda eliminado.
                            {' '}No podrá volver a jugar.
                            {' '}Los demás jugadores continúan la partida.
                        </>
                    )}
                </p>

                <button
                    type="button"
                    className="accusation-result-button"
                    onClick={victory || spectator || noWinner ? onReturnToMenu : onClose}
                >
                    {victory || spectator || noWinner ? 'Volver al menú' : 'Continuar viendo la partida'}
                </button>
            </section>
        </div>
    );
}
