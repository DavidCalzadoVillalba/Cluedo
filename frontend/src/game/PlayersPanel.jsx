import { CHARACTER_CLASSES, CHARACTER_NAMES } from './gameConstants';

export default function PlayersPanel({ players, maxPlayers, activePlayerId, username }) {
    return (
        <article className="gameplay-panel-card">
            <h2>
                <span>Jugadores</span>
                <span className="gameplay-panel-badge">{players.length}/{maxPlayers}</span>
            </h2>
            <div className="gameplay-players-list">
                {players.map((player) => {
                    const character = String(player.character ?? '').toUpperCase();
                    const isCurrentTurn = activePlayerId === player.id;
                    const isInactiveTurn = Boolean(activePlayerId) && !isCurrentTurn;
                    const isMe = player.user?.username === username;

                    return (
                        <div
                            key={player.id}
                            className={`gameplay-player-item ${isCurrentTurn ? 'is-current-turn' : ''} ${isInactiveTurn ? 'is-inactive-turn' : ''} ${isMe ? 'is-me' : ''}`}
                        >
                            <div
                                className={`gameplay-player-token ${CHARACTER_CLASSES[character] ?? ''}`}
                                title={CHARACTER_NAMES[character] ?? character}
                                aria-label={CHARACTER_NAMES[character] ?? character}
                            />
                            <div className="gameplay-player-details">
                                <span className="gameplay-player-username">
                                    {player.user?.username} {isMe && '(Tú)'}
                                </span>
                                <span className="gameplay-player-character">
                                    {CHARACTER_NAMES[character] ?? character}
                                </span>
                            </div>
                            {player.eliminated && <span className="gameplay-panel-badge">Eliminado</span>}
                            {isCurrentTurn && !player.eliminated && <span className="gameplay-panel-badge">Turno</span>}
                        </div>
                    );
                })}
            </div>
        </article>
    );
}
