
export default function SolutionNotice({ isOpen, onClose }) {
    if (!isOpen) return null;

    return (
        <div className="solution-notice-backdrop" role="presentation">
            <section className="solution-notice" role="dialog" aria-modal="true" aria-labelledby="solution-notice-title">
                <div className="solution-envelope" aria-hidden="true">
                    <div className="solution-envelope-flap" />
                    <span className="solution-envelope-seal">◆</span>
                    <span className="solution-envelope-stamp">CONFIDENCIAL</span>
                </div>
                <p className="solution-notice-eyebrow">SOBRE CONFIDENCIAL</p>
                <h2 id="solution-notice-title">La solución ya está guardada</h2>
                <p>Se ha elegido una carta de sospechoso, una de arma y una de habitación. El sobre permanecerá cerrado hasta la acusación.</p>
                <p className="solution-notice-deal">El resto de cartas ya se ha repartido entre los jugadores.</p>
                <button className="gameplay-roll-button" onClick={onClose}>
                    Ver tablero
                </button>
            </section>
        </div>
    );
}
