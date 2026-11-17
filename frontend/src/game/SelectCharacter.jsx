import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './estilos.css';
import logoCluedo from '../static/images/Cluedo_logo.png'
import api from '../services/api';
import { CHARACTERS as characters, CHARACTER_SELECT_CARDS } from './gameConstants';

const getErrorMessage = (error, fallback) =>
    typeof error.response?.data?.message === 'string'
        ? error.response.data.message
        : typeof error.response?.data === 'string' ? error.response.data : fallback;

export default function SelectCharacter() {
    const { gameCode } = useParams();
    const [selectedCharacter, setSelectedCharacter] = useState(null);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedCharacter) {
            alert('Por favor, selecciona un personaje.');
            return;
        }
        setError('');
        setIsSubmitting(true);
        try {
            const character = characters.find((item) => item.id === selectedCharacter);
            await api.post(`/games/${encodeURIComponent(gameCode)}/players`, {
                character: character.apiValue,
            });
            navigate(`/game/lobby/${gameCode}`);
        } catch (joinError) {
            const message = getErrorMessage(joinError, 'No se ha podido unir a la partida.');
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
                    <h2>SELECCIONA TU IDENTIDAD</h2>
                    <p>Elige tu personaje para la partida {gameCode}</p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="identity-section">
                        <div className="identity-header">
                            <h3>Personajes disponibles</h3>
                            <span>Solo puedes elegir los que no estén ya ocupados</span>
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
                        <button type="button" className="btn-cancel" onClick={() => navigate('/join-game')}>
                            Cancelar
                        </button>
                        <button type="submit" className="btn-create" disabled={!selectedCharacter || isSubmitting}>
                            {isSubmitting ? 'Uniéndose...' : 'Unirse a la partida'}
                        </button>
                    </div>
                    {error && <p className="lobby-inline-error" role="alert">{error}</p>}
                </form>
            </div>
        </div>
    );
}
