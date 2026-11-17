import { useEffect, useState } from "react";
import { FaCalendarAlt, FaHistory, FaHome, FaTrophy, FaUsers } from "react-icons/fa";
import { getMyGameHistory } from "../services/games";
import "./gameHistory.css";

function formatPlayedAt(value) {
    if (!value) return "Fecha no disponible";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "Fecha no disponible";
    return new Intl.DateTimeFormat("es-ES", {
        day: "2-digit", month: "2-digit", year: "numeric",
        hour: "2-digit", minute: "2-digit",
    }).format(date);
}

export default function GameHistory() {
    const [games, setGames] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

    useEffect(() => {
        let mounted = true;
        getMyGameHistory()
            .then((history) => { if (mounted) setGames(history); })
            .catch(() => { if (mounted) setError(true); })
            .finally(() => { if (mounted) setLoading(false); });
        return () => { mounted = false; };
    }, []);

    return (
        <main className="history-page">
            <section className="history-panel" aria-labelledby="history-title">
                <h1 id="history-title"><FaHistory aria-hidden="true" /> Mi historial de partidas</h1>
                {loading ? <p className="history-message" role="status">Cargando historial…</p> : null}
                {error ? <p className="history-message history-error" role="alert">No se ha podido cargar el historial. Inténtalo de nuevo más tarde.</p> : null}
                {!loading && !error && games.length === 0 ? (
                    <p className="history-message">Todavía no has participado en ninguna partida finalizada.</p>
                ) : null}
                {!loading && !error && games.length > 0 ? (
                    <div className="history-table-scroll">
                        <table className="history-table">
                            <thead><tr>
                                <th><FaHome aria-hidden="true" /> Sala</th>
                                <th><FaCalendarAlt aria-hidden="true" /> Fecha</th>
                                <th><FaUsers aria-hidden="true" /> Participantes</th>
                                <th><FaTrophy aria-hidden="true" /> Resultado</th>
                            </tr></thead>
                            <tbody>{games.map((game) => (
                                <tr key={game.id}>
                                    <td data-label="Sala">{game.name}</td>
                                    <td data-label="Fecha">{formatPlayedAt(game.playedAt)}</td>
                                    <td data-label="Participantes">{game.participants?.join(", ")}</td>
                                    <td data-label="Resultado">
                                        <span className={`history-result ${game.won ? "is-won" : "is-lost"}`}>
                                            <FaTrophy aria-hidden="true" /> {game.won ? "Ganada" : "Perdida"}
                                        </span>
                                    </td>
                                </tr>
                            ))}</tbody>
                        </table>
                    </div>
                ) : null}
            </section>
        </main>
    );
}
