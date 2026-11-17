import { getCardDisplayName, getCardImage, getCardTypeName } from './gameConstants';

export default function NeutralCardsModal({ cards, loading, error, onSelect, onClose, busy }) {
    return (
        <div className="neutral-cards-backdrop" role="presentation">
            <section className="neutral-cards-modal" role="dialog" aria-modal="true" aria-labelledby="neutral-cards-title">
                <span className="neutral-cards-eye" aria-hidden="true">◉</span>
                <h2 id="neutral-cards-title">Consulta una carta neutral</h2>
                <p className="neutral-cards-description">
                    Tu rival no puede refutar la sugerencia. Elige una carta del tablero para verla en privado.
                    Las cartas que ya consultaste seguirán reveladas para ti.
                </p>
                {error && <p className="neutral-cards-error" role="alert">{error}</p>}
                {loading ? <p className="neutral-cards-loading">Cargando cartas…</p> : (
                    <>
                    <div className="neutral-cards-grid">
                        {cards.map((item, index) => {
                            const card = item.revealedCard;
                            const image = getCardImage(card);
                            return (
                                <button
                                    type="button"
                                    className={`neutral-card-option ${card ? 'is-revealed' : ''}`}
                                    key={item.id}
                                    onClick={() => !card && onSelect(item.id)}
                                    disabled={Boolean(card) || !item.availableToConsult || busy}
                                    aria-label={card ? `Carta neutral ${index + 1}: ${getCardDisplayName(card)}, ya consultada` : `Consultar carta neutral ${index + 1}`}
                                >
                                    <span className="neutral-card-face">
                                        {card ? (
                                            <>
                                                {image && <img src={image} alt="" />}
                                                <span className="neutral-card-name">{getCardDisplayName(card)}</span>
                                                <span className="neutral-card-type">{getCardTypeName(card)}</span>
                                            </>
                                        ) : (
                                            <span className="neutral-card-back"><strong>CLUEDO</strong><span aria-hidden="true">⌕</span></span>
                                        )}
                                    </span>
                                    <span className="neutral-card-caption">Carta neutral {index + 1}</span>
                                    {card && <span className="neutral-card-private">◉ Vista privada</span>}
                                </button>
                            );
                        })}
                    </div>
                    {cards.some((item) => item.revealedCard) && cards.every((item) => !item.availableToConsult) && (
                        <p className="neutral-cards-limit">Ya has consultado una carta neutral este turno. Las demás seguirán ocultas.</p>
                    )}
                    </>
                )}
                <button type="button" className="neutral-cards-close" onClick={onClose} disabled={busy}>
                    Cerrar
                </button>
            </section>
        </div>
    );
}
