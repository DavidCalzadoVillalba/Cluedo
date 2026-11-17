import { useState, useMemo, useEffect } from 'react';
import './board.css';
import { CELL_TEXTURES } from './boardTextures';
import { getGroupNameByCellId, UNCLICKABLE_CELL_IDS } from './boardRooms';
import {
    CHARACTER_CLASSES,
    CHARACTER_NAMES,
    CHARACTER_TOKENS_3D,
    ROOM_NAMES,
    WEAPON_IMAGES,
    WEAPON_NAMES,
    WEAPON_TOKENS_3D,
    getCardImage,
    getCardDisplayName,
    getCardTypeName,
    getBonusCardInfo,
} from '../game/gameConstants';
const SLOT_POSITIONS = {
    1: ['one'],
    2: ['one', 'two'],
    3: ['one', 'two', 'four'],
    4: ['one', 'two', 'four', 'five'],
    5: ['one', 'two', 'three', 'four', 'five'],
    6: ['one', 'two', 'three', 'four', 'five', 'six'],
};

export default function Board({
    cols = 21,
    rows = 19,
    boxes = [],
    players = [],
    weapons = [],
    characterTokens = [],
    cellTypes = {},
    cellTextures = CELL_TEXTURES,
    onCellClick,
    reachableBoxIds = [],
    selectedCellId,
    activeTurn,
    initialShowIds = false,
    backgroundImage = '/images/fondo.png',
    width = 600,
    currentUsername,
    onNotebookOpen,
    onInitialRoll,
    isRollingInitial = false,
    onRollTurn,
    isRollingTurn = false,
    isMoving = false,
    secondsLeft = null,
    onFinishTurn = null,
    isFinishingTurn = false,
    isHypothesisPending = false,
    canUsePassage = false,
    onSecretPassage = null,
    isUsingPassage = false,
    currentRoom = null,
    passageDestinationRoom = null,
    animatingPlayer = null,
    myHand = [],
    myBonusCards = [],
    onExitMenu = null,
    gameCode = null,
}) {
    const [selectedCell, setSelectedCell] = useState(null);
    const [hoveredGroup, setHoveredGroup] = useState(null);
    const [showIds, setShowIds] = useState(initialShowIds);
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const destinationName = ROOM_NAMES[passageDestinationRoom] || passageDestinationRoom || 'la otra habitación';
    const currentRoomName = ROOM_NAMES[currentRoom] || currentRoom || 'habitación';

    useEffect(() => {
        if (!sidebarOpen) return;
        const handleKeyDown = (e) => {
            if (e.key === 'Escape') {
                setSidebarOpen(false);
            }
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [sidebarOpen]);

    const gridCells = useMemo(() => {
        const boxMap = new Map();
        if (boxes && boxes.length > 0) {
            boxes.forEach((b) => {
                const key = `${b.x}-${b.y}`;
                boxMap.set(key, b);
            });
        }

        const textureMap = new Map();
        if (typeof cellTextures === 'object' && cellTextures !== null) {
            Object.entries(cellTextures).forEach(([key, value]) => {
                if (!value) return;
                key.split(',').forEach((cellId) => textureMap.set(cellId.trim(), value));
            });
        }

        const list = [];
        for (let y = 0; y < rows; y++) {
            for (let x = 0; x < cols; x++) {
                const existing = boxMap.get(`${x}-${y}`);
                const sequentialId = y * cols + x + 1;
                const id = existing?.id ?? sequentialId;

                const type = cellTypes[id] || cellTypes[`${x},${y}`] || null;
                const group = getGroupNameByCellId(sequentialId);

                const texture =
                    existing?.texture ||
                    existing?.image ||
                    existing?.textureUrl ||
                    textureMap.get(String(sequentialId)) ||
                    null;

                list.push({
                    id,
                    displayId: sequentialId,
                    x,
                    y,
                    row: y,
                    col: x,
                    type,
                    boxType: existing?.type || null,
                    room: existing?.room || null,
                    group,
                    texture,
                });
            }
        }
        return list;
    }, [cols, rows, boxes, cellTypes, cellTextures]);

    const activeSelectedId = selectedCellId ?? selectedCell?.id;
    const reachableBoxIdSet = useMemo(() => new Set(reachableBoxIds), [reachableBoxIds]);
    const hasReachableCells = reachableBoxIdSet.size > 0;

    const cellCoordMap = useMemo(() => {
        const map = new Map();
        gridCells.forEach((c) => {
            map.set(`${c.x},${c.y}`, c);
        });
        return map;
    }, [gridCells]);

    const reachableGroups = useMemo(() => {
        const groups = new Set();
        gridCells.forEach((c) => {
            if (reachableBoxIdSet.has(c.id)) {
                if (c.group) groups.add(c.group);
                if (c.room) groups.add(c.room);
            }
        });
        return groups;
    }, [gridCells, reachableBoxIdSet]);

    const effectivePlayers = useMemo(() => {
        if (!animatingPlayer) return players;
        return players.map((p) => {
            if (p.id === animatingPlayer.id) {
                return {
                    ...p,
                    currentBox: {
                        ...p.currentBox,
                        x: animatingPlayer.x,
                        y: animatingPlayer.y,
                        id: animatingPlayer.boxId ?? p.currentBox?.id,
                    },
                };
            }
            return p;
        });
    }, [players, animatingPlayer]);

    const effectiveCharacterPieces = useMemo(() => {
        const ALL_CHARACTERS = ['AMAPOLA', 'RUBIO', 'BLANCO', 'PRADO', 'CELESTE', 'MORA'];
        const DEFAULT_STARTS = {
            AMAPOLA: { x: 9, y: 9 },
            RUBIO: { x: 10, y: 9 },
            BLANCO: { x: 11, y: 9 },
            PRADO: { x: 9, y: 11 },
            CELESTE: { x: 10, y: 10 },
            MORA: { x: 11, y: 11 },
        };

        const playerMap = new Map();
        effectivePlayers.forEach((p) => {
            if (p.character) {
                playerMap.set(String(p.character).toUpperCase(), p);
            }
        });

        const tokenMap = new Map();
        (characterTokens || []).forEach((t) => {
            if (t.character) {
                tokenMap.set(String(t.character).toUpperCase(), t);
            }
        });

        return ALL_CHARACTERS.map((charKey) => {
            const player = playerMap.get(charKey);
            const token = tokenMap.get(charKey);

            if (player) {
                return {
                    ...player,
                    character: charKey,
                    isPlayer: true,
                    currentBox: player.currentBox || token?.currentBox || DEFAULT_STARTS[charKey],
                };
            }

            return {
                id: `unassigned-${charKey.toLowerCase()}`,
                character: charKey,
                isPlayer: false,
                currentBox: token?.currentBox || DEFAULT_STARTS[charKey],
            };
        });
    }, [effectivePlayers, characterTokens]);

    const positionPlayerMap = useMemo(() => {
        const map = {};
        if (!players || players.length === 0) return map;
        const count = Math.min(Math.max(players.length, 1), 6);
        const positions = SLOT_POSITIONS[count] || SLOT_POSITIONS[6];
        players.slice(0, count).forEach((player, idx) => {
            const pos = positions[idx];
            if (pos) {
                map[pos] = player;
            }
        });
        return map;
    }, [players]);

    const renderDice = (player) => {
        const isActive = activeTurn?.player?.id === player.id;
        const first = isActive ? activeTurn?.diceOne : player.initialDiceOne;
        const second = isActive ? activeTurn?.diceTwo : player.initialDiceTwo;
        return (
            <span className="player-dice" aria-label={first && second ? `Dados: ${first} y ${second}` : 'Dados'}>
                <span className={`die-face ${first ? `die-${first}` : ''}`}>{first || '·'}</span>
                <span className={`die-face ${second ? `die-${second}` : ''}`}>{second || '·'}</span>
            </span>
        );
    };

    const renderPlayerSlot = (player, position) => {
        if (!player) return null;

        const isMe = Boolean(currentUsername && player.user?.username === currentUsername);
        const isInitialRollPhase = !activeTurn && players.some((p) => p.initialDiceResult == null);
        const hasRolledInitial = player.initialDiceResult != null;
        const hasActiveTurn = Boolean(activeTurn?.player?.id);
        const isActiveTurn = activeTurn?.player?.id === player.id;
        const hasRolledTurn = Boolean(activeTurn?.diceResult != null || activeTurn?.hasMoved);
        const isDimmed = hasActiveTurn && !isActiveTurn;
        const isTurnWarning = isActiveTurn && secondsLeft !== null && secondsLeft <= 10;

        const totalTurnDuration = 60;
        const ringRadius = 24.5;
        const ringCircumference = 2 * Math.PI * ringRadius;
        const currentSeconds = secondsLeft !== null ? Math.max(0, Math.min(totalTurnDuration, secondsLeft)) : totalTurnDuration;
        const elapsed = totalTurnDuration - currentSeconds;
        const ringProgress = elapsed / totalTurnDuration;
        const ringOffset = ringCircumference * (1 - ringProgress);

        const isLeftMargin = ['one', 'six', 'five'].includes(position);

        return (
            <div
                key={`slot-container-${position}-${player.id}`}
                className={`player-slot-wrapper ${isMe ? 'is-user-slot-wrapper' : ''} ${isLeftMargin ? 'is-left-slot' : 'is-right-slot'}`}
            >
                <div
                    key={`slot-${position}-${player.id}`}
                    className={`player-slot-placeholder player-slot-${position} ${isActiveTurn ? 'is-active' : ''} ${isDimmed ? 'is-dimmed' : ''} ${isTurnWarning ? 'is-warning' : ''} ${isMe ? 'is-current-user' : ''}`}
                    title={player.user?.username || 'Jugador'}
                    data-testid={`player-slot-${position}`}
                    data-player-id={player.id}
                >
                    <div className="player-slot-content">
                        <div className={`player-avatar-wrapper ${isActiveTurn ? 'has-active-turn' : ''}`}>
                            {isActiveTurn && secondsLeft !== null && (
                                <svg
                                    className={`player-avatar-ring-svg ${isTurnWarning ? 'is-warning' : ''}`}
                                    viewBox="0 0 54 54"
                                    aria-label="Progreso del tiempo de turno"
                                >
                                    <circle
                                        className="player-avatar-ring-track"
                                        cx="27"
                                        cy="27"
                                        r={ringRadius}
                                    />
                                    <circle
                                        className="player-avatar-ring-progress"
                                        cx="27"
                                        cy="27"
                                        r={ringRadius}
                                        strokeDasharray={ringCircumference}
                                        strokeDashoffset={ringOffset}
                                    />
                                </svg>
                            )}
                            <div
                                className={`player-avatar-circle ${CHARACTER_CLASSES[String(player.character ?? '').toUpperCase()] ?? ''}`}
                                title={CHARACTER_NAMES[String(player.character ?? '').toUpperCase()] ?? player.character}
                                aria-label={CHARACTER_NAMES[String(player.character ?? '').toUpperCase()] ?? player.character}
                            />
                        </div>
                        <div className="player-slot-info">
                            <span className="player-slot-name">
                                {player.user?.username || 'Jugador'}
                                {isMe && ' (Tú)'}
                            </span>
                            {player.turnOrder && !isInitialRollPhase && (
                                <span className="player-turn-order">Turno #{player.turnOrder}</span>
                            )}
                        </div>
                    </div>

                    {isInitialRollPhase ? (
                        <div className="player-slot-dice-area">
                            {isMe && !hasRolledInitial ? (
                                <button
                                    type="button"
                                    className="player-dice-action-btn"
                                    onClick={onInitialRoll}
                                    disabled={isRollingInitial}
                                    title="Pulse para tirar los dados"
                                >
                                    <img src="/images/dice.svg" alt="Dados" className="player-dice-img" />
                                    <span className="player-dice-action-text">
                                        {isRollingInitial ? 'Lanzando...' : 'Pulse para tirar los dados'}
                                    </span>
                                </button>
                            ) : hasRolledInitial ? (
                                <div className="player-dice-result-container">
                                    {renderDice(player)}
                                    <span className="player-dice-total">Total: {player.initialDiceResult}</span>
                                </div>
                            ) : (
                                <div className="player-dice-waiting">
                                    <img src="/images/dice.svg" alt="Dados" className="player-dice-img-small" />
                                    <span className="player-dice-waiting-text">Esperando tirada...</span>
                                </div>
                            )}
                        </div>
                    ) : isActiveTurn ? (
                        <div className="player-slot-dice-area">
                            {!hasRolledTurn ? (
                                isMe ? (
                                    <button
                                        type="button"
                                        className="player-dice-action-btn"
                                        onClick={onRollTurn}
                                        disabled={isRollingTurn || isMoving}
                                        title="Pulse para tirar los dados"
                                    >
                                        <img src="/images/dice.svg" alt="Dados" className="player-dice-img" />
                                        <span className="player-dice-action-text">
                                            {isRollingTurn ? 'Lanzando...' : 'Pulse para tirar los dados'}
                                        </span>
                                    </button>
                                ) : (
                                    <div className="player-dice-waiting">
                                        <img src="/images/dice.svg" alt="Dados" className="player-dice-img-small" />
                                        <span className="player-dice-waiting-text">Esperando tirada...</span>
                                    </div>
                                )
                            ) : (
                                <div className="player-dice-result-container">
                                    {renderDice(player)}
                                    {activeTurn?.diceResult != null && (
                                        <span className="player-dice-total">Total: {activeTurn.diceResult}</span>
                                    )}
                                </div>
                            )}
                            {secondsLeft !== null && (
                                <span className={`player-slot-timer ${secondsLeft <= 10 ? 'is-warning' : ''}`} title="Tiempo restante del turno">
                                    {secondsLeft}s
                                </span>
                            )}
                            {isMe && canUsePassage && onSecretPassage && (
                                <button
                                    type="button"
                                    className="player-slot-passage-btn"
                                    onClick={onSecretPassage}
                                    disabled={isUsingPassage}
                                    title={`Entrar por el pasadizo secreto hacia ${destinationName}`}
                                >
                                    {isUsingPassage ? 'Entrando...' : `🚪 Pasadizo a ${destinationName}`}
                                </button>
                            )}
                            {isMe && onFinishTurn && (
                                <button
                                    type="button"
                                    className="player-slot-finish-turn-btn"
                                    onClick={onFinishTurn}
                                    disabled={isFinishingTurn || isHypothesisPending}
                                    title={isHypothesisPending ? 'Debes esperar a que se resuelva la hipótesis para finalizar el turno' : 'Finalizar turno'}
                                >
                                    {isFinishingTurn ? 'Finalizando...' : 'Finalizar turno'}
                                </button>
                            )}
                        </div>
                    ) : (
                        <div className="player-slot-turn-area">
                            {renderDice(player)}
                        </div>
                    )}
                </div>
                {isMe && onNotebookOpen && (
                    <button
                        type="button"
                        className="user-notebook-attached-btn"
                        onClick={onNotebookOpen}
                        title="Abrir libreta de anotaciones"
                        aria-label="Abrir libreta de investigación"
                    >
                        <span className="notebook-btn-icon">📖</span>
                        <span className="notebook-btn-label">Libreta</span>
                    </button>
                )}
            </div>
        );
    };

    const handleCellClick = (cell) => {
        const activePlayerBox = activeTurn?.player?.currentBox;
        const activePlayerRoom = activePlayerBox?.room;
        const activePlayerGroup = activePlayerBox ? getGroupNameByCellId(activePlayerBox.y * cols + activePlayerBox.x + 1) : null;
        const seqId = cell.y * cols + cell.x + 1;
        if (UNCLICKABLE_CELL_IDS.has(seqId)) {
            return;
        }

        const isCurrentRoom = Boolean(
            (activePlayerRoom && (cell.room === activePlayerRoom || cell.group === activePlayerRoom)) ||
            (activePlayerGroup && (cell.group === activePlayerGroup || cell.room === activePlayerGroup))
        );
        if (isCurrentRoom) {
            return;
        }

        setSelectedCell(cell);
        if (onCellClick) {
            onCellClick(cell);
        }
    };

    return (
        <div
            className="board-page-container"
            style={backgroundImage ? { backgroundImage: `url(${backgroundImage})` } : undefined}
        >
            <header className="board-margin-top">
                <div className="top-bar-left">
                    <button
                        type="button"
                        className="top-icon-btn"
                        aria-label="Menú principal"
                        title="Menú"
                        onClick={() => setSidebarOpen((prev) => !prev)}
                    >
                        <span style={{ fontSize: '1.2rem', lineHeight: 1 }}>☰</span>
                        <span>Menú</span>
                    </button>
                    <span className="board-subtitle">Mansión</span>
                    {gameCode && (
                        <span className="board-room-code-badge" title="Código de la sala">
                            Código: <strong>{gameCode}</strong>
                        </span>
                    )}
                </div>

                <div className="top-bar-center">
                    {canUsePassage && onSecretPassage && (
                        <button
                            type="button"
                            className="board-top-passage-btn"
                            onClick={onSecretPassage}
                            disabled={isUsingPassage}
                            title={`Entrar por el pasadizo secreto de ${currentRoomName} hacia ${destinationName}`}
                        >
                            <span className="passage-badge-icon">🚪</span>
                            <span>Entrar por el pasadizo a <strong>{destinationName}</strong></span>
                        </button>
                    )}
                </div>

                <div className="top-bar-right">
                    <button
                        className={`top-icon-btn toggle-ids-btn ${showIds ? 'active' : ''}`}
                        onClick={() => setShowIds((prev) => !prev)}
                        title="Alternar visualización de ID en las casillas"
                    >
                        <span>{showIds ? 'Ocultar IDs' : 'Ver IDs'}</span>
                    </button>
                </div>
            </header>

            {sidebarOpen && (
                <div
                    className="board-sidebar-backdrop"
                    onClick={() => setSidebarOpen(false)}
                    data-testid="board-sidebar-backdrop"
                >
                    <aside
                        className="board-sidebar-drawer"
                        onClick={(e) => e.stopPropagation()}
                        aria-label="Menú del juego"
                    >
                        <div className="board-sidebar-header">
                            <div className="board-sidebar-title-row">
                                <span className="board-sidebar-icon">🏰</span>
                                <h2 className="board-sidebar-title">Menú</h2>
                            </div>
                            <button
                                type="button"
                                className="board-sidebar-close-btn"
                                onClick={() => setSidebarOpen(false)}
                                aria-label="Cerrar menú"
                                title="Cerrar menú"
                            >
                                ✕
                            </button>
                        </div>

                        <div className="board-sidebar-body">
                            <button
                                type="button"
                                className="board-sidebar-action-btn board-sidebar-exit-btn"
                                onClick={() => {
                                    setSidebarOpen(false);
                                    if (onExitMenu) {
                                        onExitMenu();
                                    } else {
                                        window.location.href = '/';
                                    }
                                }}
                            >
                                <span className="board-sidebar-btn-icon">🚪</span>
                                <div className="board-sidebar-btn-texts">
                                    <span className="board-sidebar-btn-label">Salir al menú</span>
                                    <span className="board-sidebar-btn-sub">Volver a la pantalla principal</span>
                                </div>
                            </button>
                            {onNotebookOpen && (
                                <button
                                    type="button"
                                    className="board-sidebar-action-btn"
                                    onClick={() => {
                                        setSidebarOpen(false);
                                        onNotebookOpen();
                                    }}
                                >
                                    <span className="board-sidebar-btn-icon">📖</span>
                                    <div className="board-sidebar-btn-texts">
                                        <span className="board-sidebar-btn-label">Libreta de notas</span>
                                        <span className="board-sidebar-btn-sub">Consultar pistas y sospechas</span>
                                    </div>
                                </button>
                            )}
                            {canUsePassage && onSecretPassage && (
                                <button
                                    type="button"
                                    className="board-sidebar-action-btn board-sidebar-passage-btn"
                                    onClick={() => {
                                        setSidebarOpen(false);
                                        onSecretPassage();
                                    }}
                                    disabled={isUsingPassage}
                                >
                                    <span className="board-sidebar-btn-icon">🚪</span>
                                    <div className="board-sidebar-btn-texts">
                                        <span className="board-sidebar-btn-label">Entrar por el pasadizo</span>
                                        <span className="board-sidebar-btn-sub">Ir directamente a {destinationName}</span>
                                    </div>
                                </button>
                            )}
                        </div>

                        <div className="board-sidebar-footer">
                            <button
                                type="button"
                                className="board-sidebar-cancel-btn"
                                onClick={() => setSidebarOpen(false)}
                            >
                                Reanudar partida
                            </button>
                        </div>
                    </aside>
                </div>
            )}

            <div className="board-main-layout">
                <aside className="board-side-margin board-margin-left">
                    {renderPlayerSlot(positionPlayerMap['one'], 'one')}
                    {renderPlayerSlot(positionPlayerMap['six'], 'six')}
                    {renderPlayerSlot(positionPlayerMap['five'], 'five')}
                </aside>

                <main className="board-center-wrapper">
                    <div className="board-frame">
                        <div
                            className={`board-grid ${hasReachableCells ? "has-reachable-targets" : ""}`}
                            style={{
                                '--board-cols': cols,
                                '--board-rows': rows,
                                width: typeof width === 'number' ? `${width}px` : width,
                                position: 'relative',
                            }}
                            role="grid"
                            aria-label={`Tablero de juego de ${cols} por ${rows} casillas`}
                        >
                            {gridCells.map((cell) => {
                                const activePlayerBox = activeTurn?.player?.currentBox;
                                const activePlayerRoom = activePlayerBox?.room;
                                const activePlayerGroup = activePlayerBox ? getGroupNameByCellId(activePlayerBox.y * cols + activePlayerBox.x + 1) : null;
                                const isCurrentRoom = Boolean(
                                    (activePlayerRoom && cell.room === activePlayerRoom) ||
                                    (activePlayerGroup && cell.group && cell.group === activePlayerGroup)
                                );

                                const isSelected = activeSelectedId === cell.id;
                                const isUnclickable = UNCLICKABLE_CELL_IDS.has(cell.displayId);
                                const isInRoom = !isUnclickable && Boolean(cell.group || cell.room);
                                const isReachable = !isUnclickable && !isCurrentRoom && reachableBoxIdSet.has(cell.id);
                                const isCellInReachableRoom = !isUnclickable && !isCurrentRoom && Boolean(isInRoom && (reachableGroups.has(cell.group) || (cell.room && reachableGroups.has(cell.room))));
                                const isDestination = isReachable || isCellInReachableRoom;
                                const isDimmed = hasReachableCells && !isDestination && !isSelected;
                                const isHallwayReachable = !isUnclickable && isReachable && !isInRoom;
                                const isGroupHovered = !isUnclickable && Boolean(hoveredGroup && cell.group && hoveredGroup === cell.group);

                                let edgeClasses = '';
                                if (isCellInReachableRoom) {
                                    const groupName = cell.group;
                                    const topNeighbor = cellCoordMap.get(`${cell.x},${cell.y - 1}`);
                                    const bottomNeighbor = cellCoordMap.get(`${cell.x},${cell.y + 1}`);
                                    const leftNeighbor = cellCoordMap.get(`${cell.x - 1},${cell.y}`);
                                    const rightNeighbor = cellCoordMap.get(`${cell.x + 1},${cell.y}`);

                                    if (!topNeighbor || topNeighbor.group !== groupName) {
                                        edgeClasses += ' edge-top';
                                    }
                                    if (!bottomNeighbor || bottomNeighbor.group !== groupName) {
                                        edgeClasses += ' edge-bottom';
                                    }
                                    if (!leftNeighbor || leftNeighbor.group !== groupName) {
                                        edgeClasses += ' edge-left';
                                    }
                                    if (!rightNeighbor || rightNeighbor.group !== groupName) {
                                        edgeClasses += ' edge-right';
                                    }
                                }

                                const playersOnCell = effectiveCharacterPieces.filter((piece) =>
                                (piece.currentBox?.x != null && piece.currentBox?.y != null
                                    ? (piece.currentBox.x === cell.x && piece.currentBox.y === cell.y)
                                    : (piece.currentBox?.id != null && piece.currentBox.id === cell.id)));
                                const weaponsOnCell = weapons.filter((weapon) =>
                                    (weapon.currentBox?.x != null && weapon.currentBox?.y != null ? (weapon.currentBox.x === cell.x && weapon.currentBox.y === cell.y) : (weapon.currentBox?.id != null && weapon.currentBox.id === cell.id)));

                                const typeClass = cell.type ? `cell-${cell.type.toLowerCase().replace('_', '-')}` : '';
                                const cellStyle = cell.texture
                                    ? { backgroundImage: `url(${cell.texture})` }
                                    : undefined;

                                return (
                                    <button
                                        key={`cell-${cell.x}-${cell.y}`}
                                        id={`cell-${cell.id}`}
                                        data-cell-id={cell.id}
                                        data-x={cell.x}
                                        data-y={cell.y}
                                        data-row={cell.row}
                                        data-col={cell.col}
                                        data-type={cell.type || 'EMPTY'}
                                        data-group={cell.group || ''}
                                        type="button"
                                        className={`board-cell ${typeClass} ${isInRoom ? 'is-in-room' : ''} ${isSelected ? 'selected' : ''} ${isReachable ? 'is-reachable' : ''} ${isCellInReachableRoom ? `is-room-reachable${edgeClasses}` : ''} ${isDimmed ? 'is-dimmed' : ''} ${isGroupHovered ? 'group-hovered' : ''} ${cell.texture ? 'has-texture' : ''} ${playersOnCell.length > 0 ? 'has-players' : ''} ${playersOnCell.some((p) => p.isPlayer && p.id === activeTurn?.player?.id) ? 'has-active-player' : ''}`}
                                        style={cellStyle}
                                        onClick={() => handleCellClick(cell)}
                                        onMouseEnter={() => {
                                            if (cell.group) setHoveredGroup(cell.group);
                                        }}
                                        onMouseLeave={() => setHoveredGroup(null)}
                                        title={`Casilla #${cell.id} (x: ${cell.x}, y: ${cell.y})${cell.group ? ` [Sala: ${cell.group}]` : ''}${(isHallwayReachable || isCellInReachableRoom) ? ' · Destino alcanzable' : ''}`}
                                        aria-label={`Casilla ${cell.id}, columna ${cell.x}, fila ${cell.y}${(isHallwayReachable || isCellInReachableRoom) ? ', destino alcanzable' : ''}`}
                                    >
                                        {showIds && <span className="cell-id-label">{cell.displayId}</span>}
                                        {!isInRoom && isReachable && <span className="reachable-step-indicator" aria-hidden="true" />}
                                        {(playersOnCell.length > 0 || weaponsOnCell.length > 0) && (
                                            <span className="cell-pieces" aria-label="Fichas en esta casilla">
                                                {(() => {
                                                    const isShared = (playersOnCell.length + weaponsOnCell.length) > 1;
                                                    return (
                                                        <>
                                                            {playersOnCell.map((player) => {
                                                                const character = String(player.character ?? '').toUpperCase();
                                                                const isPlayer = player.isPlayer;
                                                                const isTurnPlayer = isPlayer && activeTurn?.player?.id === player.id;
                                                                const token3d = CHARACTER_TOKENS_3D?.[character];
                                                                const isAnimating = isPlayer && animatingPlayer?.id === player.id;
                                                                const moveDx = isAnimating && animatingPlayer?.fromX != null
                                                                    ? animatingPlayer.fromX - animatingPlayer.x
                                                                    : 0;
                                                                const moveDy = isAnimating && animatingPlayer?.fromY != null
                                                                    ? animatingPlayer.fromY - animatingPlayer.y
                                                                    : 0;
                                                                const hoppingStyle = isAnimating && (moveDx !== 0 || moveDy !== 0) ? {
                                                                    '--move-from-x': moveDx,
                                                                    '--move-from-y': moveDy,
                                                                } : undefined;

                                                                const pieceClass = isPlayer ? 'cell-piece-player' : 'cell-piece-unassigned';
                                                                const pieceTitle = isPlayer
                                                                    ? `Jugador: ${CHARACTER_NAMES[character] ?? character}`
                                                                    : `Personaje: ${CHARACTER_NAMES[character] ?? character}`;
                                                                const pieceAria = isPlayer
                                                                    ? `Jugador: ${CHARACTER_NAMES[character] ?? character}`
                                                                    : `Personaje: ${CHARACTER_NAMES[character] ?? character}`;

                                                                if (token3d) {
                                                                    return (
                                                                        <span
                                                                            key={`player-${player.id}`}
                                                                            className={`cell-piece ${pieceClass} cell-piece-3d ${CHARACTER_CLASSES[character] ?? ''} ${isTurnPlayer ? 'is-active-turn' : ''} ${isTurnPlayer && secondsLeft !== null && secondsLeft <= 10 ? 'is-warning' : ''} ${isShared ? 'is-shared-cell' : ''} ${isAnimating ? 'is-hopping' : ''}`}
                                                                            style={hoppingStyle}
                                                                            title={pieceTitle}
                                                                            aria-label={pieceAria}
                                                                            role="img"
                                                                        >
                                                                            <img
                                                                                src={token3d}
                                                                                alt=""
                                                                                className="piece-3d-img"
                                                                                draggable={false}
                                                                            />
                                                                        </span>
                                                                    );
                                                                }

                                                                return (
                                                                    <span
                                                                        key={`player-${player.id}`}
                                                                        className={`cell-piece ${pieceClass} ${CHARACTER_CLASSES[character] ?? ''} ${isTurnPlayer ? 'is-active-turn' : ''} ${isTurnPlayer && secondsLeft !== null && secondsLeft <= 10 ? 'is-warning' : ''} ${isShared ? 'is-shared-cell' : ''} ${isAnimating ? 'is-hopping' : ''}`}
                                                                        style={hoppingStyle}
                                                                        title={pieceTitle}
                                                                        aria-label={pieceAria}
                                                                        role="img"
                                                                    />
                                                                );
                                                            })}
                                                            {weaponsOnCell.map((weapon) => {
                                                                const weaponName = WEAPON_NAMES[weapon.weapon] ?? weapon.weapon;
                                                                const weaponImage = WEAPON_IMAGES[weapon.weapon];
                                                                const weaponToken3d = WEAPON_TOKENS_3D?.[weapon.weapon];

                                                                if (weaponToken3d) {
                                                                    return (
                                                                        <span
                                                                            key={`weapon-${weapon.id}`}
                                                                            className={`cell-piece cell-piece-weapon cell-piece-3d cell-piece-weapon-3d ${isShared ? 'is-shared-cell' : ''}`}
                                                                            title={`Arma: ${weaponName}`}
                                                                            aria-label={`Arma: ${weaponName}`}
                                                                            role="img"
                                                                        >
                                                                            <img
                                                                                src={weaponToken3d}
                                                                                alt=""
                                                                                className="piece-3d-img piece-3d-weapon-img"
                                                                                draggable={false}
                                                                            />
                                                                        </span>
                                                                    );
                                                                }

                                                                return (
                                                                    <span
                                                                        key={`weapon-${weapon.id}`}
                                                                        className="cell-piece cell-piece-weapon"
                                                                        title={`Arma: ${weaponName}`}
                                                                        aria-label={`Arma: ${weaponName}`}
                                                                        role="img"
                                                                    >
                                                                        {weaponImage
                                                                            ? <img src={weaponImage} alt="" />
                                                                            : weaponName?.charAt(0) ?? '?'}
                                                                    </span>
                                                                );
                                                            })}
                                                        </>
                                                    );
                                                })()}
                                            </span>
                                        )}
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                </main>

                <aside className="board-side-margin board-margin-right">
                    {renderPlayerSlot(positionPlayerMap['two'], 'two')}
                    {renderPlayerSlot(positionPlayerMap['three'], 'three')}
                    {renderPlayerSlot(positionPlayerMap['four'], 'four')}
                </aside>
            </div>

            <footer className="board-margin-bottom">
                <div className="bottom-bar-left">
                    {selectedCell ? (
                        <div className="cell-info-badge" role="status">
                            Casilla seleccionada: <strong>ID #{selectedCell.id}</strong>
                            {selectedCell.group ? ` · Sala: ${selectedCell.group}` : ''}
                        </div>
                    ) : (
                        <div className="cell-info-badge" style={{ opacity: 0.7 }}>
                            Haz clic en cualquier casilla para ver su ID y coordenadas
                        </div>
                    )}
                </div>

                <div className="bottom-bar-center">
                    {(myHand.length > 0 || myBonusCards.length > 0) && (
                        <div className="board-hand-container" aria-label="Cartas en mi mano">
                            <span className="board-hand-label">Mis Cartas</span>
                            <div className="board-hand-cards">
                                {myHand.map((card, idx) => {
                                    const total = myHand.length + myBonusCards.length;
                                    const mid = (total - 1) / 2;
                                    const diff = idx - mid;
                                    const angle = diff * 3.5;
                                    const yOffset = Math.abs(diff) * 2.5;
                                    const image = getCardImage(card);
                                    const name = getCardDisplayName(card);
                                    const typeName = getCardTypeName(card);

                                    return (
                                        <div
                                            key={`board-card-${card.id || idx}`}
                                            className="board-hand-card"
                                            style={{
                                                '--card-angle': `${angle}deg`,
                                                '--card-y': `${yOffset}px`,
                                                zIndex: idx + 1,
                                            }}
                                            title={`${name} (${typeName})`}
                                            aria-label={`Carta: ${name}, ${typeName}`}
                                        >
                                            {image ? (
                                                <img
                                                    src={image}
                                                    alt={name}
                                                    className="board-hand-card-img"
                                                    draggable={false}
                                                />
                                            ) : (
                                                <div className="board-hand-card-fallback">
                                                    <span className="card-fallback-type">{typeName}</span>
                                                    <strong className="card-fallback-name">{name}</strong>
                                                </div>
                                            )}
                                        </div>
                                    );
                                })}

                                {myBonusCards.map((card, idx) => {
                                    const offsetIdx = myHand.length + idx;
                                    const total = myHand.length + myBonusCards.length;
                                    const mid = (total - 1) / 2;
                                    const diff = offsetIdx - mid;
                                    const angle = diff * 3.5;
                                    const yOffset = Math.abs(diff) * 2.5;
                                    const info = getBonusCardInfo(card);

                                    return (
                                        <div
                                            key={`board-bonus-card-${card.id || idx}`}
                                            className="board-hand-card board-hand-card-bonus"
                                            style={{
                                                '--card-angle': `${angle}deg`,
                                                '--card-y': `${yOffset}px`,
                                                zIndex: offsetIdx + 1,
                                            }}
                                            title={`Carta Bonus: ${info.name}`}
                                            aria-label={`Carta Bonus: ${info.name}`}
                                        >
                                            {info.image ? (
                                                <img
                                                    src={info.image}
                                                    alt={info.name}
                                                    className="board-hand-card-img"
                                                    draggable={false}
                                                />
                                            ) : (
                                                <div className="board-hand-card-fallback bonus-fallback">
                                                    <span className="card-fallback-type">BONUS</span>
                                                    <strong className="card-fallback-name">{info.name}</strong>
                                                </div>
                                            )}
                                            <span className="board-hand-bonus-pill">BONUS</span>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    )}
                </div>

                <div className="bottom-bar-right">
                    {canUsePassage && onSecretPassage && (
                        <button
                            type="button"
                            className="board-bottom-passage-btn"
                            onClick={onSecretPassage}
                            disabled={isUsingPassage}
                            title={`Entrar por el pasadizo secreto de ${currentRoomName} hacia ${destinationName}`}
                        >
                            <span>🚪 Pasadizo a <strong>{destinationName}</strong></span>
                        </button>
                    )}
                </div>
            </footer>
        </div>
    );
}
