import {
    CHARACTERS,
    CHARACTER_IMAGES,
    CHARACTER_SELECT_CARDS,
    ROOM_NAMES,
    ROOM_IMAGES,
    WEAPON_NAMES,
    WEAPON_IMAGES,
} from './gameConstants';

export default function SuggestionModal({
    isOpen,
    onClose,
    currentRoom,
    suggestSuspect,
    suggestWeapon,
    isSuggesting,
    onSuspectChange,
    onWeaponChange,
    onSuggest,
}) {
    if (!isOpen) return null;

    const roomName = ROOM_NAMES[currentRoom] || currentRoom;
    const roomImage = ROOM_IMAGES[currentRoom];

    return (
        <div
            className="suggestion-modal-overlay"
            role="dialog"
            aria-modal="true"
            aria-labelledby="suggestion-modal-title"
            onClick={(e) => {
                if (e.target === e.currentTarget) {
                    onClose();
                }
            }}
        >
            <div className="suggestion-modal-card">
                <button
                    type="button"
                    className="suggestion-modal-close"
                    onClick={onClose}
                    aria-label="Cerrar sugerencia"
                    title="Cerrar ventana para ver el tablero"
                >
                    ✕
                </button>

                <div className="suggestion-modal-header">
                    <div className="suggestion-room-thumbnail">
                        {roomImage ? (
                            <img src={roomImage} alt={roomName} />
                        ) : (
                            <div className="suggestion-room-fallback">🚪</div>
                        )}
                    </div>
                    <div className="suggestion-header-text">
                        <h2 id="suggestion-modal-title">Sugerencia</h2>
                        <p>
                            Estás en el <span className="suggestion-room-highlight">{roomName}</span>. Selecciona un sospechoso y un arma para formular tu sugerencia.
                        </p>
                    </div>
                </div>

                <div className="suggestion-section">
                    <h3 className="suggestion-section-title">Sospechoso</h3>
                    <div className="suggestion-choices-container">
                        <div className="suggestion-choices-grid">
                            {CHARACTERS.map((char) => {
                                const isSelected = suggestSuspect === char.apiValue;
                                const charImg = CHARACTER_SELECT_CARDS?.[char.apiValue] || CHARACTER_IMAGES[char.apiValue];
                                return (
                                    <button
                                        key={char.id}
                                        type="button"
                                        className={`suggestion-choice-card is-character ${isSelected ? 'selected' : ''}`}
                                        onClick={() => onSuspectChange(char.apiValue)}
                                        aria-label={`Sospechoso: ${char.name}`}
                                        aria-pressed={isSelected}
                                        title={`Sospechoso: ${char.name}`}
                                        disabled={isSuggesting}
                                    >
                                        <div className="suggestion-choice-img-wrap">
                                            {charImg && (
                                                <img src={charImg} alt="" draggable={false} />
                                            )}
                                        </div>
                                        <span className="suggestion-choice-label">{char.name}</span>
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                </div>

                <div className="suggestion-section">
                    <h3 className="suggestion-section-title">Arma</h3>
                    <div className="suggestion-choices-container">
                        <div className="suggestion-choices-grid">
                            {Object.entries(WEAPON_NAMES).map(([weapKey, weapName]) => {
                                const isSelected = suggestWeapon === weapKey;
                                const weapImg = WEAPON_IMAGES[weapKey];
                                return (
                                    <button
                                        key={weapKey}
                                        type="button"
                                        className={`suggestion-choice-card is-weapon ${isSelected ? 'selected' : ''}`}
                                        onClick={() => onWeaponChange(weapKey)}
                                        aria-label={`Arma: ${weapName}`}
                                        aria-pressed={isSelected}
                                        title={`Arma: ${weapName}`}
                                        disabled={isSuggesting}
                                    >
                                        <div className="suggestion-choice-img-wrap">
                                            {weapImg && (
                                                <img src={weapImg} alt="" draggable={false} />
                                            )}
                                        </div>
                                        <span className="suggestion-choice-label">{weapName}</span>
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                </div>

                <div className="suggestion-modal-footer">
                    <button
                        type="button"
                        className="suggestion-submit-button"
                        onClick={onSuggest}
                        disabled={!suggestSuspect || !suggestWeapon || isSuggesting}
                    >
                        <svg
                            className="suggestion-submit-icon"
                            viewBox="0 0 24 24"
                            width="20"
                            height="20"
                            stroke="currentColor"
                            strokeWidth="2.5"
                            fill="none"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            aria-hidden="true"
                        >
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                        <span>{isSuggesting ? 'Sugiriendo...' : 'Sugerir'}</span>
                    </button>
                </div>
            </div>
        </div>
    );
}
