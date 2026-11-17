import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import logoCluedo from '../static/images/Cluedo_logo.png';
import './estilos.css';
import useAuth from '../hooks/useAuth';
import api from '../services/api';
import { CHARACTER_NAMES, CHARACTER_CLASSES } from './gameConstants';

const getErrorMessage = (error, fallback) =>
    typeof error.response?.data?.message === 'string'
        ? error.response.data.message
        : typeof error.response?.data === 'string' ? error.response.data : fallback;

const toLobbyView = (game) => ({
    ...game,
    players: (game.players ?? []).map((player) => {
        const character = String(player.character ?? '').toUpperCase();
        return {
            ...player,
            name: player.user?.username ?? 'Jugador',
            character: CHARACTER_NAMES[character] ?? character,
            colorClass: CHARACTER_CLASSES[character] ?? '',
            isHost: player.user?.id === game.host?.id,
        };
    }),
});

const REFRESH_INTERVAL = 2000;
const isGameStarted = (status) => String(status ?? '').toUpperCase() === 'STARTED';

export default function Lobby() {
    const { gameCode } = useParams();
    const navigate = useNavigate();
    const { username } = useAuth();
    const [lobby, setLobby] = useState(null);
    const [error, setError] = useState('');
    const [starting, setStarting] = useState(false);

    const refreshLobby = useCallback(async () => {
        try {
            const response = await api.get(`/games/${encodeURIComponent(gameCode)}`);
            const game = response.data;
            if (isGameStarted(game.status)) {
                navigate(`/game/play/${encodeURIComponent(gameCode)}`);
                return;
            }
            setLobby(toLobbyView(game));
            setError('');
        } catch (loadError) {
            setError(getErrorMessage(loadError, 'No se ha podido cargar la sala de espera.'));
        }
    }, [gameCode, navigate]);

    useEffect(() => {
        refreshLobby();

        const intervalId = window.setInterval(refreshLobby, REFRESH_INTERVAL);
        return () => {
            window.clearInterval(intervalId);
        };
    }, [refreshLobby]);

    const handleStartGame = async () => {
        try {
            setStarting(true);
            setError('');
            await api.post(`/games/${encodeURIComponent(gameCode)}/start`);
            navigate(`/game/play/${encodeURIComponent(gameCode)}`);
        } catch (startError) {
            setError(getErrorMessage(startError, 'No se ha podido iniciar la partida.'));
            setStarting(false);
        }
    };

    const kickPlayer = async (player) => {
        const confirmed = window.confirm(`¿Expulsar a ${player.name} de la sala?`);
        if (!confirmed) return;

        try {
            const response = await api.delete(`/games/${encodeURIComponent(gameCode)}/players/${player.id}`);
            setLobby(toLobbyView(response.data));
            setError('');
        } catch (kickError) {
            setError(getErrorMessage(kickError, 'No se ha podido expulsar al jugador.'));
        }
    };

    if (error && !lobby) {
        return (
            <main className="create-game-page lobby-page">
                <div className="create-game-card lobby-card lobby-error" role="alert">
                    <h1>No se ha podido cargar la sala</h1>
                    <p>{error}</p>
                    <button className="btn-cancel" onClick={() => navigate('/join-game')}>
                        Volver
                    </button>
                </div>
            </main>
        );
    }

    if (!lobby) {
        return <main className="create-game-page lobby-page"><p className="lobby-loading">Cargando sala de espera...</p></main>;
    }

    const isHost = lobby.host?.username === username
        || lobby.players.some((player) => player.isHost && player.name === username);
    return (
        <main className="create-game-page lobby-page">
            <div className="lobby-brand">
                <img src={logoCluedo} alt="Cluedo" />
            </div>

            <section className="create-game-card lobby-card" aria-labelledby="lobby-title">
                <header className="lobby-header">
                    <div>
                        <p className="lobby-eyebrow">SALA DE ESPERA</p>
                        <h1 id="lobby-title">{lobby.name}</h1>
                        <p className="lobby-code">Código de partida: <strong>{gameCode}</strong></p>
                    </div>
                    <div className="lobby-capacity" aria-label={`${lobby.players.length} de ${lobby.maxPlayers} jugadores`}>
                        <strong>{lobby.players.length}/{lobby.maxPlayers}</strong>
                        <span>jugadores</span>
                    </div>
                </header>

                <div className="lobby-divider" />

                <div className="lobby-content">
                    <div className="lobby-section-heading">
                        <h2>Jugadores conectados</h2>
                        <span>Actualización automática</span>
                    </div>
                    <div className="lobby-players">
                        {lobby.players.map((player) => (
                            <article className="lobby-player-card" key={player.id}>
                                <div className={`lobby-player-avatar ${player.colorClass}`} aria-hidden="true">
                                    {player.name.charAt(0).toUpperCase()}
                                </div>
                                <div className="lobby-player-info">
                                    <h3>{player.name}</h3>
                                    <p>{player.character}</p>
                                </div>
                                {player.isHost && <span className="lobby-host-badge">Anfitrión</span>}
                                {isHost && !player.isHost && (
                                    <button
                                        type="button"
                                        className="lobby-host-badge lobby-kick-button"
                                        onClick={() => kickPlayer(player)}
                                    >
                                        Expulsar
                                    </button>
                                )}
                            </article>
                        ))}
                    </div>
                </div>

                <footer className="lobby-actions">
                    {isHost ? (
                        <>
                            {lobby.players.length < 2 && (
                                <p className="lobby-status">
                                    Se necesitan al menos 2 jugadores para poder iniciar la partida.
                                </p>
                            )}
                            <button
                                className="btn-create lobby-start-button"
                                onClick={handleStartGame}
                                disabled={lobby.players.length < 2 || starting}
                            >
                                {starting ? 'Iniciando partida...' : 'Iniciar partida'}
                            </button>
                        </>
                    ) : (
                        <p className="lobby-waiting-message">Esperando al anfitrión...</p>
                    )}
                    {error && <p className="lobby-inline-error" role="alert">{error}</p>}
                </footer>
            </section>
        </main>
    );
}
