import {
    CHARACTER_NAMES,
    CHARACTER_SELECT_CARDS,
    ROOM_NAMES,
    WEAPON_NAMES,
    getCardDisplayName,
    getCardImage,
    getCardTypeName,
} from './gameConstants';

export default function RefutedCardModal({
    isOpen,
    onClose,
    refutedSuggestion,
    onOpenNotebook,
}) {
    if (!isOpen || !refutedSuggestion || !refutedSuggestion.shownCard) return null;

    const { shownCard, refutedByPlayerName, character, weapon, room } = refutedSuggestion;

    const cardName = getCardDisplayName(shownCard);
    const cardType = getCardTypeName(shownCard);
    const cardImg = shownCard.character
        ? (CHARACTER_SELECT_CARDS?.[shownCard.character] || getCardImage(shownCard))
        : getCardImage(shownCard);

    const typeModifier = (shownCard.character || shownCard.cardType === 'CHARACTER' || shownCard.type === 'CHARACTER')
        ? 'is-character'
        : (shownCard.weapon || shownCard.cardType === 'WEAPON' || shownCard.type === 'WEAPON')
            ? 'is-weapon'
            : 'is-room';

    const suggestedCharName = CHARACTER_NAMES[character] || character;
    const suggestedWeaponName = WEAPON_NAMES[weapon] || weapon;
    const suggestedRoomName = ROOM_NAMES[room] || room;

    const matchesChar = Boolean(
        shownCard.character === character ||
        ((shownCard.cardType === 'CHARACTER' || shownCard.type === 'CHARACTER') && shownCard.name === character)
    );
    const matchesWeapon = Boolean(
        shownCard.weapon === weapon ||
        ((shownCard.cardType === 'WEAPON' || shownCard.type === 'WEAPON') && shownCard.name === weapon)
    );
    const matchesRoom = Boolean(
        shownCard.room === room ||
        ((shownCard.cardType === 'ROOM' || shownCard.type === 'ROOM') && shownCard.name === room)
    );

    return (
        <div
            className="refute-modal-overlay refuted-card-modal-overlay"
            role="dialog"
            aria-modal="true"
            aria-labelledby="refuted-card-modal-title"
            onClick={(e) => {
                if (e.target === e.currentTarget) {
                    onClose();
                }
            }}
        >
            <div className="refute-modal-card refuted-card-modal-card">
                <button
                    type="button"
                    className="refute-modal-close"
                    onClick={onClose}
                    aria-label="Cerrar aviso de refutación"
                    title="Cerrar ventana"
                >
                    ✕
                </button>

                <div className="refuted-card-modal-header">
                    <span className="refuted-card-eyebrow">HIPÓTESIS REFUTADA</span>
                    <h2 id="refuted-card-modal-title" className="refuted-card-title">
                        Tu hipótesis ha sido refutada:
                    </h2>
                    <p className="refuted-card-subtitle">
                        <strong>{refutedByPlayerName || 'Un jugador'}</strong> te ha mostrado la siguiente carta en secreto:
                    </p>
                </div>

                <div className="refuted-card-display-wrap">
                    <div className={`refuted-card-visual ${typeModifier}`} aria-label={`Carta mostrada: ${cardType} ${cardName}`}>
                        <div className="refuted-card-image-wrap">
                            {cardImg ? (
                                <img src={cardImg} alt={cardName} draggable={false} />
                            ) : (
                                <span className="refuted-card-fallback-icon" aria-hidden="true">🃏</span>
                            )}
                        </div>
                        <div className="refuted-card-badge-wrap">
                            <span className="refuted-card-category-badge">{cardType}</span>
                        </div>
                        <h3 className="refuted-card-name">{cardName}</h3>
                    </div>
                </div>

                {(suggestedCharName || suggestedWeaponName || suggestedRoomName) && (
                    <div className="refuted-card-context" role="region" aria-label="Elementos de tu hipótesis">
                        <span className="refuted-card-context-label">Tu hipótesis incluía:</span>
                        <div className="refuted-card-context-tags">
                            {suggestedCharName && (
                                <span className={`refuted-card-context-tag ${matchesChar ? 'is-matched' : ''}`}>
                                    👤 {suggestedCharName}
                                </span>
                            )}
                            {suggestedWeaponName && (
                                <span className={`refuted-card-context-tag ${matchesWeapon ? 'is-matched' : ''}`}>
                                    🗡️ {suggestedWeaponName}
                                </span>
                            )}
                            {suggestedRoomName && (
                                <span className={`refuted-card-context-tag ${matchesRoom ? 'is-matched' : ''}`}>
                                    🚪 {suggestedRoomName}
                                </span>
                            )}
                        </div>
                    </div>
                )}

                <div className="refuted-card-modal-footer">
                    {onOpenNotebook && (
                        <button
                            type="button"
                            className="refuted-card-notebook-btn"
                            onClick={() => {
                                onOpenNotebook();
                            }}
                        >
                            📓 Abrir libreta
                        </button>
                    )}
                    <button
                        type="button"
                        className="gameplay-roll-button refuted-card-dismiss-btn"
                        onClick={onClose}
                    >
                        Entendido
                    </button>
                </div>
            </div>
        </div>
    );
}

