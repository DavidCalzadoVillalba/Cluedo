import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './estilos.css';
import logoCluedo from '../static/images/Cluedo_logo.png'
import api from '../services/api';

const getErrorMessage = (error, fallback) =>
    typeof error.response?.data?.message === 'string'
        ? error.response.data.message
        : typeof error.response?.data === 'string' ? error.response.data : fallback;

export default function JoinGame() {
    const [gameCode, setGameCode] = useState('');
    const [error, setError] = useState(null);
    const navigate = useNavigate();


    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        if (!gameCode.trim()) {
            setError('Por favor, introduce un código de acceso válido.');
            return;
        }
        try {
            const normalizedCode = gameCode.trim().replace(/^#/, '').toUpperCase();
            await api.get(`/games/${encodeURIComponent(normalizedCode)}/join`);
            navigate(`/select-character/${normalizedCode}`);
        } catch (joinError) {
            const message = getErrorMessage(joinError, 'La partida no existe, ya ha comenzado o está completa.');
            setError(message);
            alert(message);
        }
    };

    return (
        <div className="create-game-page">
            <div className="cluedo-title">
                <img src={logoCluedo} />
            </div>
            <div className="create-game-card">
                <div className="card-header">
                    <h2>UNIRSE A UNA NUEVA PARTIDA</h2>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-group" style={{ marginBottom: '2.5rem' }}>
                        <label htmlFor="gameCode" style={{ textAlign: 'center', display: 'block', marginBottom: '1rem' }}>
                            INTRODUCE CÓDIGO DE LA PARTIDA
                        </label>
                        <input
                            type="text"
                            id="gameCode"
                            placeholder="EJ: #ABC123"
                            value={gameCode}
                            onChange={(e) => setGameCode(e.target.value)}
                            style={{ textAlign: 'center', letterSpacing: '2px', textTransform: 'uppercase' }}
                            required
                        />
                    </div>

                    <div className="form-actions">
                        <button type="button" className="btn-cancel" onClick={() => navigate('/')}>
                            Cancelar
                        </button>
                        <button type="submit" className="btn-create">
                            Unirse a la Partida
                        </button>
                    </div>
                    {error && <p className="lobby-inline-error" role="alert">{error}</p>}
                </form>
            </div>
        </div>
    );
}
