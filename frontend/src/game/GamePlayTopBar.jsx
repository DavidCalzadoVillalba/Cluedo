import { useState } from 'react';
import { CHARACTER_NAMES } from './gameConstants';

export default function GamePlayTopBar({
    gameName,
    gameCode,
    activePlayer,
    activeTurn,
    isMyTurn,
    secondsLeft,
    onExit,
    onNotebookOpen,
}) {
    const [sidebarOpen, setSidebarOpen] = useState(false);

    return (
        <>
            <header className="gameplay-topbar">
                <div className="gameplay-topbar-left">
                    <button
                        type="button"
                        className="gameplay-menu-btn"
                        onClick={() => setSidebarOpen(true)}
                        aria-label="Abrir menú"
                        title="Abrir menú"
                    >
                        <span className="gameplay-menu-icon" aria-hidden="true">☰</span>
                        <span className="gameplay-menu-text">Menú</span>
                    </button>
                    <div className="gameplay-topbar-info">
                        <h1>{gameName}</h1>
                        <span className="gameplay-code-badge">
                            Código: <strong>{gameCode}</strong>
                        </span>
                    </div>
                </div>

                <div className={`gameplay-turn-badge ${isMyTurn ? 'gameplay-turn-active' : ''}`}>
                    {activePlayer ? (
                        <>
                            <span>
                                Turno #{activeTurn?.turnNumber}: <strong>{activePlayer.user?.username}</strong> ({CHARACTER_NAMES[activePlayer.character] || activePlayer.character}){isMyTurn ? ' (Tú)' : ''}
                            </span>
                            {secondsLeft !== null && (
                                <span className={`gameplay-timer-badge ${secondsLeft <= 10 ? 'is-warning' : ''}`} title="Tiempo restante del turno">
                                    <strong>{secondsLeft}s</strong>
                                </span>
                            )}
                        </>
                    ) : (
                        <span>Ronda inicial: tirada de dados</span>
                    )}
                </div>

                <div className="gameplay-topbar-right">
                    <button
                        type="button"
                        className="btn-cancel gameplay-topbar-exit-btn"
                        onClick={onExit}
                        title="Salir al menú"
                    >
                        Salir al menú
                    </button>
                </div>
            </header>

            {sidebarOpen && (
                <div
                    className="gameplay-sidebar-backdrop"
                    role="presentation"
                    onClick={() => setSidebarOpen(false)}
                >
                    <aside
                        className="gameplay-sidebar"
                        role="dialog"
                        aria-modal="true"
                        aria-label="Menú de partida"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="gameplay-sidebar-header">
                            <div className="gameplay-sidebar-title-wrap">
                                <span className="gameplay-sidebar-eyebrow">PARTIDA EN CURSO</span>
                                <h2 className="gameplay-sidebar-title">Menú</h2>
                            </div>
                            <button
                                type="button"
                                className="gameplay-sidebar-close"
                                onClick={() => setSidebarOpen(false)}
                                aria-label="Cerrar menú"
                                title="Cerrar menú"
                            >
                                ✕
                            </button>
                        </div>

                        <div className="gameplay-sidebar-info">
                            <div className="gameplay-sidebar-info-row">
                                <span className="gameplay-sidebar-info-label">Partida:</span>
                                <strong className="gameplay-sidebar-info-value">{gameName || 'Cluedo'}</strong>
                            </div>
                            <div className="gameplay-sidebar-info-row">
                                <span className="gameplay-sidebar-info-label">Código:</span>
                                <span className="gameplay-sidebar-code">{gameCode}</span>
                            </div>
                        </div>

                        <nav className="gameplay-sidebar-nav">
                            <button
                                type="button"
                                className="gameplay-sidebar-nav-item is-resume"
                                onClick={() => setSidebarOpen(false)}
                            >
                                <span className="gameplay-sidebar-icon" aria-hidden="true">▶</span>
                                <span>Continuar partida</span>
                            </button>

                            {onNotebookOpen && (
                                <button
                                    type="button"
                                    className="gameplay-sidebar-nav-item"
                                    onClick={() => {
                                        setSidebarOpen(false);
                                        onNotebookOpen();
                                    }}
                                >
                                    <span className="gameplay-sidebar-icon" aria-hidden="true">📓</span>
                                    <span>Libreta de investigación</span>
                                </button>
                            )}

                            <button
                                type="button"
                                className="gameplay-sidebar-nav-item is-exit"
                                onClick={() => {
                                    setSidebarOpen(false);
                                    onExit();
                                }}
                            >
                                <span className="gameplay-sidebar-icon" aria-hidden="true">🚪</span>
                                <span>Salir al menú</span>
                            </button>
                        </nav>
                    </aside>
                </div>
            )}
        </>
    );
}
