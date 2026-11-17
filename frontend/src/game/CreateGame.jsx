import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './estilos.css';
import logoCluedo from '../static/images/Cluedo_logo.png';
import api from '../services/api';
import { CHARACTERS as characters, CHARACTER_SELECT_CARDS } from './gameConstants';

const getErrorMessage = (error, fallback) =>
    typeof error.response?.data?.message === 'string'
        ? error.response.data.message
        : typeof error.response?.data === 'string' ? error.response.data : fallback;

export default function CreateGame() {
    const [gameName, setGameName] = useState('');
    const [maxPlayers, setMaxPlayers] = useState(4);
    const [selectedCharacter, setSelectedCharacter] = useState(null);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedCharacter) {
            return;
        }

        setError('');
        setIsSubmitting(true);
        try {
            const character = characters.find((item) => item.id === selectedCharacter);
            const response = await api.post('/games', {
                name: gameName,
                maxPlayers: Number(maxPlayers),
            }, {
                params: { character: character.apiValue },
            });
            const game = response.data;
            navigate(`/game/lobby/${game.code}`);
        } catch (createError) {
            const message = getErrorMessage(createError, 'No se ha podido crear la partida.');
            setError(message);
            alert(message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="create-game-page">
            <div className="cluedo-title">
                <img src={logoCluedo} />
            </div>

            <div className="create-game-card">
                <div className="card-header">
                    <h2>CONFIGURAR NUEVA PARTIDA</h2>
                    <p>Establece los detalles de la sesión antes de entrar en la mansión.</p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="gameName">Nombre de la sala</label>
                            <input
                                type="text"
                                id="gameName"
                                placeholder="Ej. El Misterio de Tudor"
                                value={gameName}
                                onChange={(e) => setGameName(e.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="maxPlayers">Límite de participantes</label>
                            <select
                                id="maxPlayers"
                                value={maxPlayers}
                                onChange={(e) => setMaxPlayers(e.target.value)}
                            >
                                {[2, 3, 4, 5, 6].map(num => (
                                    <option key={num} value={num}>{num} jugadores</option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div className="identity-section">
                        <div className="identity-header">
                            <h3>Selecciona tu identidad</h3>
                            <span>Se bloquea para otros jugadores al confirmar</span>
                        </div>
                        <div className="characters-grid">
                            {characters.map((char) => {
                                const selectCard = CHARACTER_SELECT_CARDS?.[char.apiValue];
                                const isSelected = selectedCharacter === char.id;
                                return (
                                    <div
                                        key={char.id}
                                        className={`character-card char-${char.id} ${selectCard ? 'has-card-image' : ''} ${isSelected ? 'selected' : ''}`}
                                        onClick={() => setSelectedCharacter(char.id)}
                                        title={char.name}
                                        aria-label={`Seleccionar ${char.name}`}
                                        role="button"
                                        tabIndex={0}
                                        onKeyDown={(e) => {
                                            if (e.key === 'Enter' || e.key === ' ') {
                                                e.preventDefault();
                                                setSelectedCharacter(char.id);
                                            }
                                        }}
                                    >
                                        {selectCard ? (
                                            <img
                                                src={selectCard}
                                                alt={char.name}
                                                className="character-card-img"
                                                draggable={false}
                                            />
                                        ) : (
                                            <>
                                                <div className={`character-color ${char.colorClass}`}></div>
                                                <p>{char.name}</p>
                                            </>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    <div className="form-actions">
                        <button type="button" className="btn-cancel" onClick={() => navigate('/')}>
                            Cancelar
                        </button>
                        <button type="submit" className="btn-create" disabled={!selectedCharacter || isSubmitting}>
                            {isSubmitting ? 'Creando...' : 'Crear Partida'}
                        </button>
                    </div>
                    {error && <p className="lobby-inline-error" role="alert">{error}</p>}
                </form>
            </div>
        </div>
    );
}
