import { useEffect, useState } from 'react';
import api from '../services/api';
import './investigationNotebook.css';

const SECTIONS = [
    {
        id: 'suspects',
        label: 'Sospechosos',
        icon: '♟',
        items: [
            ['prado', 'Prado'], ['rubio', 'Rubio'], ['celeste', 'Celeste'],
            ['mora', 'Mora'], ['amapola', 'Amapola'], ['blanco', 'Blanco'],
        ],
    },
    {
        id: 'weapons',
        label: 'Armas',
        icon: '⚒',
        items: [
            ['herramienta', 'Herramienta'], ['candelabro', 'Candelabro'], ['cuchillo', 'Puñal'],
            ['pistola', 'Pistola'], ['tuberia', 'Tubería de plomo'], ['cuerda', 'Cuerda'],
        ],
    },
    {
        id: 'rooms',
        label: 'Habitaciones',
        icon: '⌂',
        items: [
            ['bano', 'Baño'], ['estudio', 'Estudio'], ['comedor', 'Comedor'],
            ['juegos', 'Sala de juegos'], ['garaje', 'Garaje'], ['dormitorio', 'Dormitorio'],
            ['salon', 'Salón'], ['cocina', 'Cocina'], ['patio', 'Patio'],
        ],
    },
];

const HAND_CARD_IDS = {
    CHARACTER: {
        AMAPOLA: 'amapola', RUBIO: 'rubio', BLANCO: 'blanco',
        PRADO: 'prado', CELESTE: 'celeste', MORA: 'mora',
    },
    WEAPON: {
        LLAVE_INGLESA: 'herramienta', CANDELABRO: 'candelabro', CUCHILLO: 'cuchillo',
        PISTOLA: 'pistola', TUBERIA_PLOMO: 'tuberia', CUERDA: 'cuerda',
    },
    ROOM: {
        BATHROOM: 'bano', STUDY: 'estudio', DINING_ROOM: 'comedor',
        GAME_ROOM: 'juegos', GARAGE: 'garaje', BEDROOM: 'dormitorio',
        LOUNGE: 'salon', KITCHEN: 'cocina', PATIO: 'patio',
    },
};

const MARK_ITEM_IDS = {
    SUSPECT: HAND_CARD_IDS.CHARACTER,
    WEAPON: HAND_CARD_IDS.WEAPON,
    ROOM: HAND_CARD_IDS.ROOM,
};

const VALUE_TO_STATUS = {
    BLANK: '',
    X: 'cross',
    QUESTION: 'question',
    CONFIRMED: 'confirmed',
};

const STATUS_TO_VALUE = {
    '': 'BLANK',
    cross: 'X',
    question: 'QUESTION',
    confirmed: 'CONFIRMED',
};

function getNotebookEntries(marks) {
    return Object.fromEntries(marks.flatMap((mark) => {
        const cardValue = mark.character || mark.weapon || mark.room;
        const itemId = MARK_ITEM_IDS[mark.category]?.[cardValue];
        return itemId ? [[itemId, { id: mark.id, status: VALUE_TO_STATUS[mark.value] }]] : [];
    }));
}

export default function InvestigationNotebook({ gameCode, myHand = [], isOpen, onClose }) {
    const [openItem, setOpenItem] = useState(null);
    const [entries, setEntries] = useState({});
    const [isLoading, setIsLoading] = useState(false);
    const [savingItem, setSavingItem] = useState(null);
    const [error, setError] = useState('');
    const handCardIds = new Set(myHand.map((card) => {
        const category = String(card.cardType || '').toUpperCase();
        const cardValue = card.character || card.weapon || card.room;
        return HAND_CARD_IDS[category]?.[String(cardValue || '').toUpperCase()];
    }).filter(Boolean));

    useEffect(() => {
        if (!isOpen || !gameCode) return undefined;
        let isCurrentRequest = true;
        setIsLoading(true);
        setEntries({});
        setError('');
        api.get(`/games/${encodeURIComponent(gameCode)}/notebook/marks`)
            .then((response) => {
                if (isCurrentRequest) setEntries(getNotebookEntries(response.data));
            })
            .catch((requestError) => {
                if (!isCurrentRequest) return;
                const message = requestError.response?.data?.message;
                setError(typeof message === 'string' ? message : 'No se han podido cargar las marcas de la libreta.');
            })
            .finally(() => {
                if (isCurrentRequest) setIsLoading(false);
            });
        return () => {
            isCurrentRequest = false;
        };
    }, [gameCode, isOpen]);

    useEffect(() => {
        if (!isOpen) return undefined;
        const closeOnEscape = (event) => {
            if (event.key === 'Escape') onClose();
        };
        window.addEventListener('keydown', closeOnEscape);
        return () => window.removeEventListener('keydown', closeOnEscape);
    }, [isOpen, onClose]);

    const updateEntry = async (id, status) => {
        const mark = entries[id];
        if (!mark) return;
        setSavingItem(id);
        setError('');
        try {
            const response = await api.put(
                `/games/${encodeURIComponent(gameCode)}/notebook/marks/${mark.id}`,
                { value: STATUS_TO_VALUE[status] },
            );
            setEntries((current) => ({
                ...current,
                [id]: { ...current[id], status: VALUE_TO_STATUS[response.data.value] },
            }));
            setOpenItem(null);
        } catch (requestError) {
            const message = requestError.response?.data?.message;
            setError(typeof message === 'string' ? message : 'No se ha podido guardar la marca de la libreta.');
        } finally {
            setSavingItem(null);
        }
    };

    if (!isOpen) return null;

    return (
        <>
            <div
                className="notebook-backdrop"
                onClick={onClose}
                data-testid="notebook-backdrop"
                aria-hidden="true"
            />
            <aside
                className="investigation-notebook is-open"
                aria-label="Libreta de investigación"
            >
                <header className="notebook-header">
                    <div>
                        <span className="notebook-kicker">DP1 · MANSIÓN</span>
                        <h2>Libreta de investigación</h2>
                    </div>
                    <button type="button" className="notebook-close" onClick={onClose} aria-label="Cerrar libreta">×</button>
                </header>

                <div className="notebook-page">
                    {error && <p className="notebook-error" role="alert">{error}</p>}
                    {isLoading && <p role="status">Cargando marcas…</p>}
                    {SECTIONS.map((section) => (
                        <section className="notebook-category" key={section.id}>
                            <h3><span aria-hidden="true">{section.icon}</span> {section.id === 'suspects' ? '¿QUIÉN?' : section.id === 'weapons' ? '¿CON QUÉ?' : '¿DÓNDE?'}</h3>
                            <div className="notebook-entries">
                                {section.items.map(([id, label]) => {
                                    const isInHand = handCardIds.has(id);
                                    const status = isInHand ? 'cross' : entries[id]?.status || '';
                                    const menuOpen = openItem === id;
                                    return (
                                        <div className={`notebook-entry ${menuOpen ? 'has-open-menu' : ''}`} key={id}>
                                            <span className="notebook-entry-name">{label}</span>
                                            <button
                                                type="button"
                                                className={`notebook-mark ${status ? `is-${status}` : ''}`}
                                                disabled={isInHand || isLoading || !entries[id]}
                                                onClick={() => setOpenItem(menuOpen ? null : id)}
                                                aria-label={`${label}: ${isInHand ? 'carta en tu mano, descartada automáticamente' : status === 'cross' ? 'descartado' : status === 'question' ? 'duda' : status === 'confirmed' ? 'confirmada como parte de la solución' : 'sin marcar'}. ${isInHand ? '' : 'Cambiar marca'}`}
                                                aria-expanded={menuOpen}
                                                title={isInHand ? 'La tienes en tu mano: descartada automáticamente' : 'Elegir marca'}
                                            >
                                                {status === 'cross' ? '×' : status === 'question' ? '?' : ''}
                                                {status === 'confirmed' ? '✓' : null}
                                            </button>
                                            {menuOpen && (
                                                <div className="notebook-mark-menu" role="group" aria-label={`Marca para ${label}`}>
                                                    <button type="button" disabled={savingItem === id} className="mark-choice mark-cross" onClick={() => updateEntry(id, 'cross')} aria-label={`Descartar ${label}`} title="Descartar">×</button>
                                                    <button type="button" disabled={savingItem === id} className="mark-choice mark-question" onClick={() => updateEntry(id, 'question')} aria-label={`Marcar duda en ${label}`} title="Tengo dudas">?</button>
                                                    <button type="button" disabled={savingItem === id} className="mark-choice mark-confirmed" onClick={() => updateEntry(id, 'confirmed')} aria-label={`Confirmar ${label} como parte de la solución`} title="Confirmada en la solución">✓</button>
                                                    <button type="button" disabled={savingItem === id} className="mark-choice mark-clear" onClick={() => updateEntry(id, '')} aria-label={`Quitar marca de ${label}`} title="Quitar marca" />
                                                </div>
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                        </section>
                    ))}
                </div>

                <footer className="notebook-footer">
                    <span>Notas privadas · guardadas en la partida</span>
                </footer>
            </aside>
        </>
    );
}
