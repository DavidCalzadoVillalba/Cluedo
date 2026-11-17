import {
    ROOM_NAMES,
    getBonusCardInfo,
    getCardClass,
    getCardDisplayName,
    getCardImage,
    getCardTypeName,
} from './gameConstants';

export default function PlayerHand({
    myHand,
    myBonusCards,
    faceUpCards,
    isMyTurn,
    activeTurn,
    activeSuggestion,
    myPlayerId,
    selectedTeleportRoom,
    onTeleportRoomChange,
    usingBonusCardId,
    onUseBonusCard,
    onPeek,
}) {
    const allCardCount = myHand.length + myBonusCards.length;

    return (
        <article className="gameplay-panel-card">
            <h2>
                <span>Mi Mano</span>
                <span className="gameplay-panel-badge">{allCardCount} cartas</span>
            </h2>
            <div className="gameplay-cards-container">
                {myHand.length === 0 ? (
                    myBonusCards.length === 0 && <p className="gameplay-empty-cards">No tienes cartas asignadas.</p>
                ) : (
                    <div className="gameplay-cards-grid">
                        {myHand.map((card) => (
                            <div
                                key={`hand-${card.id}`}
                                className={`gameplay-card-item ${getCardClass(card)}`}
                            >
                                <span className="gameplay-card-type-tag">{getCardTypeName(card)}</span>
                                {getCardImage(card) && (
                                    <img
                                        className="gameplay-card-image"
                                        src={getCardImage(card)}
                                        alt={getCardDisplayName(card)}
                                    />
                                )}
                            </div>
                        ))}
                    </div>
                )}

                {myBonusCards.length > 0 && (
                    <div className="gameplay-cards-grid">
                        {myBonusCards.map((card) => {
                            const info = getBonusCardInfo(card);
                            const canPeekNow = card.effect === 'PEEK_CARD'
                                && activeSuggestion?.status === 'REFUTED'
                                && activeSuggestion.peekAvailable
                                && activeSuggestion.suggestedByPlayerId !== myPlayerId
                                && activeSuggestion.refutedByPlayerId !== myPlayerId;
                            const canUseNow = isMyTurn && (
                                (card.effect === 'EXTRA_TURN' && activeSuggestion?.status !== 'PENDING')
                                || (activeTurn?.diceResult == null
                                    && !activeTurn?.hasMoved
                                    && card.effect === 'EXTRA_DICE_6')
                            ) || canPeekNow;

                            return (
                                <article
                                    key={`bonus-${card.id}`}
                                    className={`gameplay-card-item gameplay-bonus-card ${canUseNow ? 'is-usable' : 'is-unavailable'}`}
                                >
                                    <span className="gameplay-card-type-tag">Carta bonus</span>
                                    {info.image && <img className="gameplay-card-image" src={info.image} alt="" />}
                                    <strong className="gameplay-card-name">{info.name}</strong>
                                    <p>{info.description}</p>
                                    {card.effect === 'TELEPORT_ANY_ROOM' && (
                                        <select
                                            aria-label="Habitación de destino"
                                            value={selectedTeleportRoom}
                                            onChange={(event) => onTeleportRoomChange(event.target.value)}
                                            disabled={!canUseNow}
                                        >
                                            <option value="">Elige habitación</option>
                                            {Object.entries(ROOM_NAMES)
                                                .filter(([room]) => room !== 'CENTER')
                                                .map(([room, name]) => <option key={room} value={room}>{name}</option>)}
                                        </select>
                                    )}
                                    <button
                                        type="button"
                                        className="gameplay-bonus-use-button"
                                        disabled={!canUseNow || usingBonusCardId === card.id}
                                        onClick={() => (card.effect === 'PEEK_CARD' ? onPeek(card) : onUseBonusCard(card))}
                                    >
                                        {usingBonusCardId === card.id
                                            ? 'Usando...'
                                            : card.effect === 'PEEK_CARD'
                                                ? 'Ver la carta mostrada'
                                                : ['EXTRA_DICE_6', 'EXTRA_TURN'].includes(card.effect)
                                                    ? 'Jugar ahora'
                                                    : 'Usar carta'}
                                    </button>
                                    {!canUseNow && (
                                        <span className="gameplay-bonus-status">
                                            {card.effect === 'PEEK_CARD'
                                                ? 'Disponible después de una refutación, si no eres quien sugirió ni quien refutó.'
                                                : 'Disponible durante tu turno, antes de tirar los dados.'}
                                        </span>
                                    )}
                                </article>
                            );
                        })}
                    </div>
                )}

                {faceUpCards.length > 0 && (
                    <div className="gameplay-face-up-section">
                        <h2>
                            <span>Cartas Boca Arriba (Mesa)</span>
                            <span className="gameplay-panel-badge">{faceUpCards.length}</span>
                        </h2>
                        <div className="gameplay-cards-grid">
                            {faceUpCards.map((card) => (
                                <div
                                    key={`faceup-${card.id}`}
                                    className={`gameplay-card-item ${getCardClass(card)}`}
                                >
                                    <span className="gameplay-card-type-tag">{getCardTypeName(card)}</span>
                                    {getCardImage(card) && (
                                        <img
                                            className="gameplay-card-image"
                                            src={getCardImage(card)}
                                            alt={getCardDisplayName(card)}
                                        />
                                    )}
                                    <strong className="gameplay-card-name">{getCardDisplayName(card)}</strong>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </article>
    );
}
