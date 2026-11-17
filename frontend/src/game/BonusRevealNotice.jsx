import { getCardDisplayName, getCardTypeName } from './gameConstants';

export default function BonusRevealNotice({
    bonusCardEvent,
    pendingBonusReveal,
    publicBonusReveals,
    myHand,
    selectedRevealCardId,
    usingBonusCardId,
    onRevealCardChange,
    onReveal,
}) {
    const isRevealing = pendingBonusReveal && usingBonusCardId === `reveal-${pendingBonusReveal.id}`;

    return (
        <>
            {!bonusCardEvent && pendingBonusReveal && (
                <div className="solution-notice-backdrop" role="presentation">
                    <section className="solution-notice bonus-card-notice" role="dialog" aria-modal="true" aria-labelledby="reveal-card-title">
                        <p className="solution-notice-eyebrow">CARTA BONUS</p>
                        <h2 id="reveal-card-title">Debes revelar una carta</h2>
                        <p>{pendingBonusReveal.requesterPlayerName} ha usado «Haz que alguien revele una carta». Elige una carta de tu mano; todos los jugadores podrán verla.</p>
                        <label className="bonus-card-event-field">
                            Carta que vas a revelar
                            <select
                                value={selectedRevealCardId}
                                onChange={(event) => onRevealCardChange(event.target.value)}
                            >
                                <option value="">Elige una carta</option>
                                {myHand.map((card) => (
                                    <option key={card.id} value={card.id}>
                                        {getCardTypeName(card)}: {getCardDisplayName(card)}
                                    </option>
                                ))}
                            </select>
                        </label>
                        <button
                            type="button"
                            className="gameplay-roll-button"
                            onClick={onReveal}
                            disabled={!selectedRevealCardId || isRevealing}
                        >
                            {isRevealing ? 'Revelando...' : 'Revelar a todos'}
                        </button>
                    </section>
                </div>
            )}
            {publicBonusReveals.length > 0 && (
                <section className="gameplay-public-reveals" aria-live="polite">
                    {publicBonusReveals.map((reveal) => (
                        <p className="gameplay-suggestion-summary" key={reveal.id}>
                            <strong>{reveal.targetPlayerName}</strong> ha revelado públicamente la carta{' '}
                            <strong>{getCardTypeName(reveal.revealedCard)}: {getCardDisplayName(reveal.revealedCard)}</strong>.
                        </p>
                    ))}
                </section>
            )}
        </>
    );
}
