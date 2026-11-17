import { useState } from "react";
import { Alert, Table } from "reactstrap";
import * as gamesApi from "../../services/games";
import useFetchState from "../../hooks/useFetchState";
import useErrorModal from "../../hooks/useErrorModal";
import "./activeGames.css";

const statusLabels = {
    WAITING: "En espera",
    STARTED: "En curso",
    ENDED: "Finalizada",
};

export default function ActiveGamesAdmin() {
    const { errorModal, showError } = useErrorModal();
    const [forbidden, setForbidden] = useState(false);
    const [games] = useFetchState([], gamesApi.getAllGames, [], {
        onError: (message) => {
            const accessDenied = message.includes("Rol de Administrador");
            setForbidden(accessDenied);
            showError(message);
        },
    });

    return (
        <main className="active-games-page">
            <h1>Partidas <span className="active-games-title-red">en</span> curso</h1>
            {forbidden && <Alert color="danger" role="alert">Necesitas el Rol de Administrador para consultar la lista de partidas</Alert>}
            {errorModal}
            <div className="active-games-table-wrap">
                <Table responsive hover className="active-games-table">
                    <thead>
                        <tr>
                            <th scope="col">Sala</th>
                            <th scope="col">Participantes</th>
                            <th scope="col">Límite</th>
                            <th scope="col">Jugadores</th>
                            <th scope="col">Estado</th>
                        </tr>
                    </thead>
                    <tbody>
                        {games.map((game) => (
                            <tr key={game.id} className={game.status === "ENDED" ? "game-ended" : ""}>
                                <td>{game.name}</td>
                                <td>{game.participantCount} / {game.maxPlayers}</td>
                                <td>{game.maxPlayers}</td>
                                <td>{game.players.length ? game.players.join(", ") : "–"}</td>
                                <td><span className={`game-status game-status-${game.status.toLowerCase()}`}>{statusLabels[game.status] ?? game.status}</span></td>
                            </tr>
                        ))}
                        {!games.length && !forbidden && (
                            <tr><td colSpan="5" className="text-center">No hay partidas registradas</td></tr>
                        )}
                    </tbody>
                </Table>
            </div>
        </main>
    );
}
