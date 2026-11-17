import { useState, useEffect } from 'react';
import SuggestionModal from './SuggestionModal';
import RefutationModal from './RefutationModal';
import {
    CHARACTER_NAMES,
    CHARACTER_IMAGES,
    CHARACTER_SELECT_CARDS,
    ROOM_NAMES,
    WEAPON_NAMES,
    getCardDisplayName,
    getCardTypeName,
} from './gameConstants';

export default function SuggestionPanel({
    canSuggest,
    currentRoom,
    suggestSuspect,
    suggestWeapon,
    isSuggesting,
    onSuspectChange,
    onWeaponChange,
    onSuggest,
    hasSuggested,
    activeSuggestion,
    isHypothesisPending,
    isMyTurnToRefute,
    isMySuggestion,
    matchingCards = [],
    selectedRefuteCardId,
    isRefuting,
    onRefuteCardChange,
    onRefute,
    skipRefutationBonusCard,
    onPass,
    myPlayerId,
    activePlayerName,
    usingBonusCardId,
    hasPeekCard,
    onPeek,
    myHand = [],
    players = [],
    hasMoved = false,
    diceResult = null,
    onOpenRefutedCardModal,
}) {
    const isChoosingAfterRoll = Boolean(diceResult != null && !hasMoved);
    const [isModalOpen, setIsModalOpen] = useState(!isChoosingAfterRoll);
    const [isRefuteModalOpen, setIsRefuteModalOpen] = useState(true);

    useEffect(() => {
        if (canSuggest) {
            if (hasMoved || diceResult == null) {
                setIsModalOpen(true);
            } else {
                setIsModalOpen(false);
            }
        }
    }, [canSuggest, currentRoom, hasMoved, diceResult]);

    useEffect(() => {
        if (isHypothesisPending && isMyTurnToRefute) {
            setIsRefuteModalOpen(true);
            if (matchingCards.length === 1 && !selectedRefuteCardId) {
                onRefuteCardChange?.(matchingCards[0].id);
            }
        }
    }, [isHypothesisPending, isMyTurnToRefute, activeSuggestion?.id]);

    if (!canSuggest && !(hasSuggested && activeSuggestion)) return null;

    const suggesterName = activeSuggestion?.suggestedByPlayerName || activePlayerName;
    const roomName = ROOM_NAMES[currentRoom] || currentRoom;

    // Suggester info for HU-017 refutation modal header
    const suggesterPlayer = players.find(
        (p) => p.id === activeSuggestion?.suggestedByPlayerId || p.user?.username === suggesterName
    );
    const suggesterCharKey = suggesterPlayer?.character;
    const charKeyUpper = String(suggesterCharKey || '').toUpperCase();
    const suggesterAvatarImg = CHARACTER_SELECT_CARDS[charKeyUpper] || CHARACTER_IMAGES[charKeyUpper] || null;

    return (
        <>
            {/* HU-016 Suggestion Formulation Modal */}
            <SuggestionModal
                isOpen={Boolean(canSuggest && isModalOpen)}
                onClose={() => setIsModalOpen(false)}
                currentRoom={currentRoom}
                suggestSuspect={suggestSuspect}
                suggestWeapon={suggestWeapon}
                isSuggesting={isSuggesting}
                onSuspectChange={onSuspectChange}
                onWeaponChange={onWeaponChange}
                onSuggest={onSuggest}
            />

            {/* Minimized button when suggestion modal is closed */}
            {canSuggest && !isModalOpen && (
                <div className="gameplay-suggestion-panel gameplay-choice-panel">
                    <div className="gameplay-suggestion-header">
                        <strong>
                            {isChoosingAfterRoll
                                ? '¿Qué deseas hacer en tu turno?'
                                : 'Formular Hipótesis'}
                        </strong>
                        <span className="gameplay-suggestion-room">
                            Habitación: <b>{roomName}</b>
                            {diceResult != null && (
                                <> · Tirada: <b>{diceResult}</b></>
                            )}
                        </span>
                    </div>
                    <div className="gameplay-suggestion-controls">
                        <button
                            type="button"
                            className="gameplay-roll-button"
                            onClick={() => setIsModalOpen(true)}
                        >
                            🔍 Formular hipótesis en {roomName}
                        </button>
                        {isChoosingAfterRoll && (
                            <span className="gameplay-roll-hint">
                                O pulsa una casilla iluminada en el tablero para moverte ({diceResult} casillas).
                            </span>
                        )}
                    </div>
                </div>
            )}

            {hasSuggested && activeSuggestion && (
                <>
                    <div className="gameplay-suggestion-summary">
                        <span>
                            Hipótesis del turno: <strong>{CHARACTER_NAMES[activeSuggestion.character] || activeSuggestion.character}</strong> con <strong>{WEAPON_NAMES[activeSuggestion.weapon] || activeSuggestion.weapon}</strong> en <strong>{ROOM_NAMES[activeSuggestion.room] || activeSuggestion.room}</strong>
                        </span>
                    </div>

                    {/* HU-017 Refutation Modal */}
                    <RefutationModal
                        isOpen={Boolean(isHypothesisPending && isMyTurnToRefute && isRefuteModalOpen)}
                        onClose={() => setIsRefuteModalOpen(false)}
                        activeSuggestion={activeSuggestion}
                        matchingCards={matchingCards}
                        selectedRefuteCardId={selectedRefuteCardId}
                        isRefuting={isRefuting}
                        onRefuteCardChange={onRefuteCardChange}
                        onRefute={onRefute}
                        skipRefutationBonusCard={skipRefutationBonusCard}
                        onPass={onPass}
                        myHand={myHand}
                        suggesterName={suggesterName}
                        suggesterAvatarImg={suggesterAvatarImg}
                    />

                    {/* Minimized button when refutation modal is closed */}
                    {isHypothesisPending && isMyTurnToRefute && !isRefuteModalOpen && (
                        <div className="gameplay-suggestion-panel">
                            <div className="gameplay-suggestion-header">
                                <strong>Tu turno para refutar</strong>
                                <span className="gameplay-suggestion-room">
                                    Hipótesis de: <b>{suggesterName || 'Jugador activo'}</b>
                                </span>
                            </div>
                            <div className="gameplay-suggestion-controls">
                                <button
                                    type="button"
                                    className="gameplay-roll-button"
                                    onClick={() => setIsRefuteModalOpen(true)}
                                >
                                    Abrir ventana de refutación ({suggesterName})
                                </button>
                            </div>
                        </div>
                    )}

                    {isHypothesisPending && !isMyTurnToRefute && (
                        <span className="gameplay-roll-hint">
                            {isMySuggestion
                                ? `Esperando a que ${activeSuggestion.currentResponderName || 'el siguiente jugador'} intente refutar tu hipótesis...`
                                : `Turno de ${activeSuggestion.currentResponderName || 'otro jugador'} para intentar refutar la hipótesis...`}
                        </span>
                    )}

                    {activeSuggestion.status === 'REFUTED' && (
                        <div className="gameplay-suggestion-summary">
                            {isMySuggestion ? (
                                activeSuggestion.shownCard ? (
                            <span>
                                Tu hipótesis ha sido refutada.{' '}
                                {onOpenRefutedCardModal && (
                                    <button
                                        type="button"
                                        className="gameplay-refuted-reopen-btn"
                                        onClick={onOpenRefutedCardModal}
                                    >
                                        👁️ Ver carta mostrada
                                    </button>
                                )}
                            </span>
                            ) : (
                            <span><strong>Tu hipótesis ha sido refutada.</strong></span>
                            )
                            ) : (activeSuggestion.refutedByPlayerId === myPlayerId || activeSuggestion.currentResponder?.id === myPlayerId) ? (
                            <span>Has refutado la hipótesis mostrándole una carta en secreto a <strong>{suggesterName}</strong>.</span>
                            ) : activeSuggestion.shownCard ? (
                            <span>
                                Has usado «Echa un vistazo» y has visto la carta{' '}
                                <strong>{getCardTypeName(activeSuggestion.shownCard)}: {getCardDisplayName(activeSuggestion.shownCard)}</strong>.
                            </span>
                            ) : (
                            <span>La hipótesis de <strong>{suggesterName}</strong> ha sido refutada por <strong>{activeSuggestion.refutedByPlayerName || 'otro jugador'}</strong>.</span>
                            )}
                        </div>
                    )}

                    {activeSuggestion.status === 'REFUTED'
                        && activeSuggestion.peekAvailable
                        && activeSuggestion.refutedByPlayerId !== myPlayerId
                        && activeSuggestion.suggestedByPlayerId !== myPlayerId
                        && hasPeekCard
                        && (
                            <button
                                type="button"
                                className="gameplay-bonus-use-button"
                                onClick={onPeek}
                                disabled={Boolean(usingBonusCardId)}
                            >
                                Echa un vistazo a la carta
                            </button>
                        )}

                    {activeSuggestion.status === 'NOT_REFUTED' && (
                        <div className="gameplay-suggestion-summary">
                            <span><strong>Nadie ha podido refutar la hipótesis</strong> formulada por {suggesterName}.</span>
                        </div>
                    )}
                </>
            )}
        </>
    );
}
