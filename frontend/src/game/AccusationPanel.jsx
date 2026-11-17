import { useState } from 'react';
import { CHARACTER_NAMES, ROOM_NAMES, WEAPON_NAMES } from './gameConstants';

const CARD_ROOMS = Object.entries(ROOM_NAMES).filter(([room]) => room !== 'CENTER');

export default function AccusationPanel({
    canAccuse,
    character,
    weapon,
    room,
    isAccusing,
    onCharacterChange,
    onWeaponChange,
    onRoomChange,
    onAccuse,
}) {
    const [confirming, setConfirming] = useState(false);

    if (!canAccuse) return null;

    const selectionComplete = Boolean(character && weapon && room);

    const reviewAccusation = () => {
        if (!selectionComplete || isAccusing) return;
        setConfirming(true);
    };

    const correctAccusation = () => {
        if (isAccusing) return;
        setConfirming(false);
    };

    const submitAccusation = () => {
        if (isAccusing) return;
        setConfirming(false);
        onAccuse();
    };

    return (
        <div className="gameplay-suggestion-panel">
            <div className="gameplay-suggestion-header">
                <strong>Acusación final</strong>
                <span className="gameplay-suggestion-room">
                    Estás en el <b>Centro</b>
                </span>
            </div>
            {confirming ? (
                <div className="gameplay-suggestion-summary" role="dialog" aria-label="Confirmar acusación">
                    <span>
                        Vas a acusar a <strong>{CHARACTER_NAMES[character]}</strong> con{' '}
                        <strong>{WEAPON_NAMES[weapon]}</strong> en <strong>{ROOM_NAMES[room]}</strong>.
                        Si fallas, quedarás eliminado y no podrás volver a moverte ni formular hipótesis.
                    </span>
                    <div className="gameplay-suggestion-controls">
                        <button
                            type="button"
                            className="gameplay-roll-button"
                            onClick={submitAccusation}
                            disabled={isAccusing}
                        >
                            {isAccusing ? 'Acusando...' : 'Confirmar acusación'}
                        </button>
                        <button
                            type="button"
                            className="gameplay-secret-passage-button"
                            onClick={correctAccusation}
                            disabled={isAccusing}
                        >
                            Corregir
                        </button>
                    </div>
                </div>
            ) : (
                <div className="gameplay-suggestion-controls">
                    <select
                        aria-label="Seleccionar sospechoso de la acusación"
                        value={character}
                        onChange={(event) => onCharacterChange(event.target.value)}
                        disabled={isAccusing}
                    >
                        <option value="">Elige sospechoso...</option>
                        {Object.entries(CHARACTER_NAMES).map(([key, name]) => (
                            <option key={key} value={key}>{name}</option>
                        ))}
                    </select>
                    <select
                        aria-label="Seleccionar arma de la acusación"
                        value={weapon}
                        onChange={(event) => onWeaponChange(event.target.value)}
                        disabled={isAccusing}
                    >
                        <option value="">Elige arma...</option>
                        {Object.entries(WEAPON_NAMES).map(([key, name]) => (
                            <option key={key} value={key}>{name}</option>
                        ))}
                    </select>
                    <select
                        aria-label="Seleccionar habitación de la acusación"
                        value={room}
                        onChange={(event) => onRoomChange(event.target.value)}
                        disabled={isAccusing}
                    >
                        <option value="">Elige habitación...</option>
                        {CARD_ROOMS.map(([key, name]) => (
                            <option key={key} value={key}>{name}</option>
                        ))}
                    </select>
                    <button
                        type="button"
                        className="gameplay-roll-button"
                        onClick={reviewAccusation}
                        disabled={!selectionComplete || isAccusing}
                    >
                        Formular acusación final
                    </button>
                </div>
            )}
        </div>
    );
}
