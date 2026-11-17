import {
    CHARACTER_NAMES,
    CHARACTER_IMAGES,
    CHARACTER_SELECT_CARDS,
    ROOM_NAMES,
    ROOM_IMAGES,
    WEAPON_NAMES,
    WEAPON_IMAGES,
    cardMatchesSuggestion,
    getCardDisplayName,
    getCardImage,
    getCardTypeName,
} from './gameConstants';

export default function RefutationModal({
    isOpen,
    onClose,
    activeSuggestion,
    matchingCards = [],
    selectedRefuteCardId,
    isRefuting,
    onRefuteCardChange,
    onRefute,
    skipRefutationBonusCard,
    onPass,
    myHand = [],
    suggesterName,
    suggesterAvatarImg,
}) {
    if (!isOpen || !activeSuggestion) return null;

    const suggestedCharKey = activeSuggestion.character;
    const suggestedWeaponKey = activeSuggestion.weapon;
    const suggestedRoomKey = activeSuggestion.room;

    const suggestedCharName = CHARACTER_NAMES[suggestedCharKey] || suggestedCharKey || 'Sospechoso';
    const suggestedCharImg = CHARACTER_SELECT_CARDS?.[suggestedCharKey] || CHARACTER_IMAGES[suggestedCharKey];

    const suggestedWeaponName = WEAPON_NAMES[suggestedWeaponKey] || suggestedWeaponKey || 'Arma';
    const suggestedWeaponImg = WEAPON_IMAGES[suggestedWeaponKey];

    const suggestedRoomName = ROOM_NAMES[suggestedRoomKey] || suggestedRoomKey || 'Habitación';
    const suggestedRoomImg = ROOM_IMAGES[suggestedRoomKey];

    const displayHand = (myHand && myHand.length > 0) ? myHand : matchingCards;
    const effectiveSelectedCardId = selectedRefuteCardId || (matchingCards.length === 1 ? matchingCards[0].id : null);

    return (
        <div
            className="refute-modal-overlay"
            role="dialog"
            aria-modal="true"
            aria-labelledby="refute-modal-title"
            onClick={(e) => {
                if (e.target === e.currentTarget) {
                    onClose();
                }
            }}
        >
            <div className="refute-modal-card">
                <button
                    type="button"
                    className="refute-modal-close"
                    onClick={onClose}
                    aria-label="Cerrar refutación"
                    title="Cerrar ventana para ver el tablero"
                >
                    ✕
                </button>

                <div className="refute-modal-header">
                    <div className="refute-avatar-wrap">
                        {suggesterAvatarImg ? (
                            <img
                                src={suggesterAvatarImg}
                                alt={suggesterName}
                                className="refute-avatar-img"
                            />
                        ) : (
                            <div className="refute-avatar-fallback">👤</div>
                        )}
                    </div>
                    <div className="refute-header-text">
                        <h2 id="refute-modal-title" className="refute-title">
                            <strong>{suggesterName}</strong> ha hecho una sugerencia.
                        </h2>
                        <p className="refute-subtitle">Ahora es tu turno de refutar la teoría.</p>
                    </div>
                </div>

                {/* 3 Suggested items in the style of the suggestion cards */}
                <div
                    className="refute-suggested-row"
                    role="region"
                    aria-label="Elementos de la sugerencia"
                >
                    <div className="refute-suggested-card-container">
                        <div className="suggestion-choice-card is-character is-suggested">
                            <div className="suggestion-choice-img-wrap">
                                {suggestedCharImg && (
                                    <img src={suggestedCharImg} alt="" draggable={false} />
                                )}
                            </div>
                            <span className="suggestion-choice-label">{suggestedCharName}</span>
                        </div>
                        <span className="refute-card-category-label">Sospechoso</span>
                    </div>

                    <div className="refute-suggested-card-container">
                        <div className="suggestion-choice-card is-weapon is-suggested">
                            <div className="suggestion-choice-img-wrap">
                                {suggestedWeaponImg && (
                                    <img src={suggestedWeaponImg} alt="" draggable={false} />
                                )}
                            </div>
                            <span className="suggestion-choice-label">{suggestedWeaponName}</span>
                        </div>
                        <span className="refute-card-category-label">Arma</span>
                    </div>

                    <div className="refute-suggested-card-container">
                        <div className="suggestion-choice-card is-room is-suggested">
                            <div className="suggestion-choice-img-wrap">
                                {suggestedRoomImg && (
                                    <img src={suggestedRoomImg} alt="" draggable={false} />
                                )}
                            </div>
                            <span className="suggestion-choice-label">{suggestedRoomName}</span>
                        </div>
                        <span className="refute-card-category-label">Habitación</span>
                    </div>
                </div>

                <p className="refute-instruction-text">
                    {matchingCards.length > 0
                        ? 'Selecciona una carta de tu mano para refutar la sugerencia:'
                        : 'No tienes ninguna carta que coincida con la sugerencia.'}
                </p>

                {/* Hand cards rendered with the exact same suggestion card styling */}
                <div
                    className="suggestion-choices-container refute-hand-container"
                    role="region"
                    aria-label="Cartas de tu mano"
                >
                    <div className="refute-hand-grid">
                        {displayHand.map((card) => {
                            const isMatch = matchingCards.some((mc) => mc.id === card.id)
                                || cardMatchesSuggestion(card, activeSuggestion);
                            const isSelected = isMatch && card.id === effectiveSelectedCardId;
                            const cardImg = card.character
                                ? (CHARACTER_SELECT_CARDS?.[card.character] || getCardImage(card))
                                : getCardImage(card);
                            const cardName = getCardDisplayName(card);
                            const cardType = getCardTypeName(card);
                            const typeModifier = card.character
                                ? 'is-character'
                                : card.weapon
                                    ? 'is-weapon'
                                    : 'is-room';

                            if (isMatch) {
                                return (
                                    <button
                                        key={card.id}
                                        type="button"
                                        className={`suggestion-choice-card ${typeModifier} is-match is-refute-match ${isSelected ? 'selected is-selected' : ''}`}
                                        onClick={() => onRefuteCardChange(card.id)}
                                        aria-label={`Refutar con ${cardType}: ${cardName}`}
                                        aria-pressed={isSelected}
                                        title={`Refutar con ${cardType}: ${cardName}`}
                                        disabled={isRefuting}
                                    >
                                        <div className="suggestion-choice-img-wrap">
                                            {cardImg ? (
                                                <img src={cardImg} alt="" draggable={false} />
                                            ) : (
                                                <span className="refute-hand-card-fallback">🃏</span>
                                            )}
                                        </div>
                                        <span className="suggestion-choice-label">{cardName}</span>
                                    </button>
                                );
                            }

                            return (
                                <div
                                    key={card.id}
                                    className={`suggestion-choice-card ${typeModifier} is-disabled is-refute-disabled`}
                                    aria-disabled="true"
                                    title={`${cardName} (no coincide con la sugerencia)`}
                                >
                                    <div className="suggestion-choice-img-wrap">
                                        {cardImg ? (
                                            <img src={cardImg} alt="" draggable={false} />
                                        ) : (
                                            <span className="refute-hand-card-fallback">🃏</span>
                                        )}
                                    </div>
                                    <span className="suggestion-choice-label">{cardName}</span>
                                </div>
                            );
                        })}
                    </div>
                </div>

                <div className="refute-modal-footer">
                    {matchingCards.length > 0 ? (
                        <>
                            <button
                                type="button"
                                className="suggestion-submit-button refute-submit-button"
                                onClick={onRefute}
                                disabled={!effectiveSelectedCardId || isRefuting}
                            >
                                <svg
                                    className="refute-submit-icon"
                                    viewBox="0 0 24 24"
                                    width="20"
                                    height="20"
                                    stroke="currentColor"
                                    strokeWidth="2.2"
                                    fill="none"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    aria-hidden="true"
                                >
                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                                    <circle cx="12" cy="12" r="3" />
                                </svg>
                                <span>{isRefuting ? 'Mostrando...' : 'Mostrar carta'}</span>
                            </button>
                            {skipRefutationBonusCard && (
                                <button
                                    type="button"
                                    className="refute-bonus-pass-button"
                                    onClick={() => onPass(true)}
                                    disabled={isRefuting}
                                    title="Usar carta bonus para pasar sin refutar"
                                >
                                    {isRefuting ? 'Usando bonus...' : 'Usar carta bonus (Pasar)'}
                                </button>
                            )}
                        </>
                    ) : (
                        <button
                            type="button"
                            className="refute-pass-button"
                            onClick={() => onPass(false)}
                            disabled={isRefuting}
                        >
                            {isRefuting ? 'Pasando...' : 'No puedo refutar (Pasar)'}
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}
