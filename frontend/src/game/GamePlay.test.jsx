import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import GamePlay from './GamePlay';
import api from '../services/api';

vi.mock('../hooks/useAuth', () => ({
    default: () => ({ username: 'player1' }),
}));

vi.mock('../board/Board', () => ({
    default: ({ onCellClick, reachableBoxIds, onRollTurn, activeTurn }) => {
        const bonusBox = { id: 181, x: 12, y: 8, type: 'BONUS', room: null };
        const normalBox = { id: 182, x: 12, y: 8, type: 'PATH', room: null };
        return (
            <div>
                <button
                    type="button"
                    disabled={!reachableBoxIds.includes(bonusBox.id)}
                    onClick={() => onCellClick(bonusBox)}
                >
                    Mover a casilla bonus
                </button>
                <button
                    type="button"
                    disabled={!reachableBoxIds.includes(normalBox.id)}
                    onClick={() => onCellClick(normalBox)}
                >
                    Mover a casilla normal
                </button>
                {onRollTurn && (
                    <button
                        type="button"
                        disabled={activeTurn?.diceResult != null || activeTurn?.hasMoved}
                        onClick={onRollTurn}
                    >
                        Pulse para tirar los dados
                    </button>
                )}
                {activeTurn?.diceResult != null && (
                    <span>Resultado: {activeTurn.diceResult}</span>
                )}
            </div>
        );
    },
}));

vi.mock('./InvestigationNotebook', () => ({ default: () => null }));

vi.mock('../services/api', () => ({
    default: { get: vi.fn(), post: vi.fn() },
}));

describe('GamePlay bonus card flow', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('shows the drawn card on reaching a bonus cell and keeps it in hand when waiting', async () => {
        const bonusCard = { id: 901, effect: 'EXTRA_DICE_6', immediate: false };
        const player = {
            id: 1,
            character: 'AMAPOLA',
            initialDiceResult: 6,
            user: { username: 'player1' },
            currentBox: { id: 180, x: 11, y: 8, type: 'PATH', room: null },
        };
        const initialState = {
            name: 'Test',
            code: 'TEST01',
            maxPlayers: 3,
            status: 'STARTED',
            players: [player],
            board: { boxes: [player.currentBox, { id: 181, x: 12, y: 8, type: 'BONUS', room: null }] },
            activeTurn: {
                id: 51,
                turnNumber: 1,
                status: 'ACTIVE',
                player,
                diceResult: 1,
                hasMoved: false,
                expiresAt: null,
            },
            myHand: [],
            myBonusCards: [],
            faceUpCards: [],
            weaponTokens: [],
            solutionSaved: false,
        };
        const updatedState = {
            ...initialState,
            myBonusCards: [bonusCard],
            activeTurn: { ...initialState.activeTurn, hasMoved: true },
        };
        let hasMoved = false;

        api.get.mockImplementation((url) => {
            if (url === '/games/TEST01') {
                return Promise.resolve({ data: hasMoved ? updatedState : initialState });
            }
            if (url === '/turns/51/reachable-boxes') {
                return Promise.resolve({
                    data: [{ id: 181, x: 12, y: 8, type: 'BONUS', room: null }],
                });
            }
            throw new Error(`Unexpected GET ${url}`);
        });
        api.post.mockImplementation((url) => {
            if (url === '/turns/51/move') {
                hasMoved = true;
                return Promise.resolve({
                    data: { landedOnBonus: true, drawnBonusCard: bonusCard },
                });
            }
            if (url === '/turns/51/finish') {
                return Promise.resolve({ data: {} });
            }
            throw new Error(`Unexpected POST ${url}`);
        });

        render(
            <MemoryRouter initialEntries={['/games/TEST01']}>
                <Routes>
                    <Route path="/games/:gameCode" element={<GamePlay />} />
                </Routes>
            </MemoryRouter>,
        );

        const moveButton = await screen.findByRole('button', { name: 'Mover a casilla bonus' });
        await waitFor(() => expect(moveButton).toBeEnabled());
        fireEvent.click(moveButton);

        expect(await screen.findByRole('dialog')).toBeInTheDocument();
        expect(screen.getByText('¡HAS CAÍDO EN UNA CASILLA BONUS!')).toBeInTheDocument();
        expect(screen.getByRole('heading', { name: 'Suma 6' })).toBeInTheDocument();
        expect(screen.getAllByRole('button', { name: 'Jugar ahora' })[0]).toBeEnabled();
        fireEvent.click(screen.getByRole('button', { name: 'Guardar carta' }));

        await waitFor(() => {
            expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
            expect(api.post).toHaveBeenCalledWith('/turns/51/finish');
        });
        expect(screen.getByText('Suma 6')).toBeInTheDocument();
        expect(document.querySelector('.gameplay-bonus-card img')).toHaveAttribute(
            'src',
            '/images/cartasbonus/suma6.png',
        );
    });

    it('plays the drawn Suma 6 card and closes its event dialog', async () => {
        const bonusCard = { id: 905, effect: 'EXTRA_DICE_6', immediate: false };
        const player = {
            id: 1,
            character: 'AMAPOLA',
            initialDiceResult: 6,
            user: { username: 'player1' },
            currentBox: { id: 180, x: 11, y: 8, type: 'PATH', room: null },
        };
        const beforeMove = {
            name: 'Test',
            code: 'TEST01',
            maxPlayers: 3,
            status: 'STARTED',
            players: [player],
            board: { boxes: [player.currentBox] },
            activeTurn: {
                id: 51,
                turnNumber: 1,
                status: 'ACTIVE',
                player,
                diceResult: 8,
                hasMoved: false,
                expiresAt: null,
            },
            myHand: [],
            myBonusCards: [],
            faceUpCards: [],
            weaponTokens: [],
            solutionSaved: false,
        };
        const afterMove = {
            ...beforeMove,
            activeTurn: { ...beforeMove.activeTurn, hasMoved: true },
            myBonusCards: [bonusCard],
        };
        const afterBonusCard = {
            ...afterMove,
            activeTurn: { ...afterMove.activeTurn, diceResult: 6, hasMoved: false },
            myBonusCards: [],
        };
        let moved = false;
        let bonusCardPlayed = false;
        api.get.mockImplementation((url) => {
            if (url === '/games/TEST01') {
                return Promise.resolve({
                    data: bonusCardPlayed ? afterBonusCard : moved ? afterMove : beforeMove,
                });
            }
            if (url === '/turns/51/reachable-boxes') {
                return Promise.resolve({
                    data: [{ id: 181, x: 12, y: 8, type: 'BONUS', room: null }],
                });
            }
            throw new Error(`Unexpected GET ${url}`);
        });
        api.post.mockImplementation((url) => {
            if (url === '/turns/51/move') {
                moved = true;
                return Promise.resolve({ data: { landedOnBonus: true, drawnBonusCard: bonusCard } });
            }
            if (url === '/turns/51/bonus-cards/905/use') {
                bonusCardPlayed = true;
                return Promise.resolve({ data: {} });
            }
            throw new Error(`Unexpected POST ${url}`);
        });

        render(
            <MemoryRouter initialEntries={['/games/TEST01']}>
                <Routes>
                    <Route path="/games/:gameCode" element={<GamePlay />} />
                </Routes>
            </MemoryRouter>,
        );

        fireEvent.click(await screen.findByRole('button', { name: 'Mover a casilla bonus' }));
        await screen.findByRole('dialog');
        fireEvent.click(screen.getAllByRole('button', { name: 'Jugar ahora' })[0]);

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/turns/51/bonus-cards/905/use', null, undefined);
            expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
            expect(screen.getByText('Resultado: 6')).toBeInTheDocument();
            expect(screen.getByRole('button', { name: 'Mover a casilla bonus' })).toBeEnabled();
        });
    });

    it('starts the extra turn immediately after playing Turno extra', async () => {
        const player = {
            id: 1,
            character: 'AMAPOLA',
            initialDiceResult: 6,
            user: { username: 'player1' },
            currentBox: { id: 180, x: 11, y: 8, type: 'PATH', room: null },
        };
        const initialState = {
            name: 'Test',
            code: 'TEST01',
            maxPlayers: 3,
            status: 'STARTED',
            players: [player],
            board: { boxes: [player.currentBox] },
            activeTurn: {
                id: 51,
                turnNumber: 1,
                status: 'ACTIVE',
                player,
                diceResult: null,
                hasMoved: false,
                expiresAt: null,
            },
            myHand: [],
            myBonusCards: [{ id: 902, effect: 'EXTRA_TURN', immediate: false }],
            faceUpCards: [],
            weaponTokens: [],
            solutionSaved: false,
        };
        const extraTurnState = {
            ...initialState,
            activeTurn: { ...initialState.activeTurn, id: 52, turnNumber: 2 },
            myBonusCards: [],
        };
        let extraTurnStarted = false;
        api.get.mockImplementation((url) => {
            if (url === '/games/TEST01') {
                return Promise.resolve({ data: extraTurnStarted ? extraTurnState : initialState });
            }
            throw new Error(`Unexpected GET ${url}`);
        });
        api.post.mockImplementation((url) => {
            if (url === '/turns/51/bonus-cards/902/use') return Promise.resolve({ data: {} });
            if (url === '/turns/51/finish') {
                extraTurnStarted = true;
                return Promise.resolve({ data: extraTurnState.activeTurn });
            }
            throw new Error(`Unexpected POST ${url}`);
        });

        render(
            <MemoryRouter initialEntries={['/games/TEST01']}>
                <Routes>
                    <Route path="/games/:gameCode" element={<GamePlay />} />
                </Routes>
            </MemoryRouter>,
        );

        fireEvent.click(await screen.findByRole('button', { name: 'Jugar ahora' }));

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/turns/51/finish');
            expect(screen.getByRole('button', { name: /pulse para tirar los dados/i })).toBeEnabled();
        });
        expect(api.post.mock.invocationCallOrder[1]).toBeGreaterThan(api.post.mock.invocationCallOrder[0]);
    });

    it.each([
        {
            effect: 'EXTRA_QUESTION',
            cardId: 903,
            selectOptions: [
                ['Sospechoso', 'AMAPOLA'],
                ['Arma', 'PISTOLA'],
                ['Habitación', 'LOUNGE'],
            ],
            expectedUrl: '/games/TEST01/suggestions/bonus-question',
        },
        {
            effect: 'TELEPORT_ANY_ROOM',
            cardId: 904,
            selectOptions: [['Habitación de destino', 'LOUNGE']],
            expectedUrl: '/turns/51/bonus-cards/904/use',
        },
    ])('closes the $effect event after playing it, even while the game state is stale', async ({
        effect,
        cardId,
        selectOptions,
        expectedUrl,
    }) => {
        const player = {
            id: 1,
            character: 'AMAPOLA',
            initialDiceResult: 6,
            user: { username: 'player1' },
            currentBox: { id: 180, x: 11, y: 8, type: 'PATH', room: null },
        };
        const staleState = {
            name: 'Test',
            code: 'TEST01',
            maxPlayers: 3,
            status: 'STARTED',
            players: [player],
            board: { boxes: [player.currentBox] },
            activeTurn: {
                id: 51,
                turnNumber: 1,
                status: 'ACTIVE',
                player,
                diceResult: null,
                hasMoved: false,
                expiresAt: null,
            },
            myHand: [],
            myBonusCards: [{ id: cardId, effect, immediate: true }],
            faceUpCards: [],
            weaponTokens: [],
            solutionSaved: false,
        };
        api.get.mockResolvedValue({ data: staleState });
        api.post.mockResolvedValue({ data: {} });

        render(
            <MemoryRouter initialEntries={['/games/TEST01']}>
                <Routes>
                    <Route path="/games/:gameCode" element={<GamePlay />} />
                </Routes>
            </MemoryRouter>,
        );

        for (const [label, value] of selectOptions) {
            fireEvent.change(await screen.findByLabelText(label), { target: { value } });
        }
        fireEvent.click(await screen.findByRole('button', { name: 'Jugar ahora' }));

        await waitFor(() => {
            if (effect === 'EXTRA_QUESTION') {
                expect(api.post).toHaveBeenCalledWith(expectedUrl, {
                    bonusCardId: cardId,
                    character: 'AMAPOLA',
                    weapon: 'PISTOLA',
                    room: 'LOUNGE',
                });
            } else {
                expect(api.post).toHaveBeenCalledWith(expectedUrl, null, { params: { room: 'LOUNGE' } });
            }
            expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        });
    });

    it('automatically finishes the turn after moving to a normal path cell', async () => {
        const player = {
            id: 1,
            character: 'AMAPOLA',
            initialDiceResult: 6,
            user: { username: 'player1' },
            currentBox: { id: 180, x: 11, y: 8, type: 'PATH', room: null },
        };
        const normalBox = { id: 182, x: 12, y: 8, type: 'PATH', room: null };
        const initialState = {
            name: 'Test',
            code: 'TEST01',
            maxPlayers: 3,
            status: 'STARTED',
            players: [player],
            board: { boxes: [player.currentBox, normalBox] },
            activeTurn: {
                id: 51,
                turnNumber: 1,
                status: 'ACTIVE',
                player,
                diceResult: 1,
                hasMoved: false,
                expiresAt: null,
            },
            myHand: [],
            myBonusCards: [],
            faceUpCards: [],
            weaponTokens: [],
            solutionSaved: false,
        };
        let moved = false;
        api.get.mockImplementation((url) => {
            if (url === '/games/TEST01') {
                return Promise.resolve({
                    data: moved ? { ...initialState, activeTurn: { ...initialState.activeTurn, hasMoved: true } } : initialState,
                });
            }
            if (url === '/turns/51/reachable-boxes') {
                return Promise.resolve({ data: [normalBox] });
            }
            throw new Error(`Unexpected GET ${url}`);
        });
        api.post.mockImplementation((url) => {
            if (url === '/turns/51/move') {
                moved = true;
                return Promise.resolve({ data: { landedOnBonus: false } });
            }
            if (url === '/turns/51/finish') {
                return Promise.resolve({ data: {} });
            }
            throw new Error(`Unexpected POST ${url}`);
        });

        render(
            <MemoryRouter initialEntries={['/games/TEST01']}>
                <Routes>
                    <Route path="/games/:gameCode" element={<GamePlay />} />
                </Routes>
            </MemoryRouter>,
        );

        const moveBtn = await screen.findByRole('button', { name: 'Mover a casilla normal' });
        fireEvent.click(moveBtn);

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/turns/51/move', null, { params: { boxId: 182 } });
            expect(api.post).toHaveBeenCalledWith('/turns/51/finish');
        });
    });

    it('automatically finishes the turn after a suggestion is resolved', async () => {
        vi.useFakeTimers({ shouldAdvanceTime: true });
        const player = {
            id: 1,
            character: 'AMAPOLA',
            initialDiceResult: 6,
            user: { username: 'player1' },
            currentBox: { id: 100, x: 5, y: 5, type: 'ROOM', room: 'LOUNGE' },
        };
        const resolvedSuggestion = {
            id: 42,
            character: 'AMAPOLA',
            weapon: 'PISTOLA',
            room: 'LOUNGE',
            status: 'REFUTED',
            suggestedByPlayerId: 1,
            refutedByPlayerName: 'player2',
            shownCard: { id: 10, name: 'PISTOLA', type: 'WEAPON' },
        };
        const resolvedState = {
            name: 'Test',
            code: 'TEST01',
            maxPlayers: 3,
            status: 'STARTED',
            players: [player],
            board: { boxes: [player.currentBox] },
            activeTurn: {
                id: 51,
                turnNumber: 1,
                status: 'ACTIVE',
                player,
                diceResult: 4,
                hasMoved: true,
                expiresAt: null,
                suggestion: resolvedSuggestion,
            },
            activeSuggestion: resolvedSuggestion,
            myHand: [],
            myBonusCards: [],
            faceUpCards: [],
            weaponTokens: [],
            solutionSaved: false,
        };
        api.get.mockResolvedValue({ data: resolvedState });
        api.post.mockResolvedValue({ data: {} });

        render(
            <MemoryRouter initialEntries={['/games/TEST01']}>
                <Routes>
                    <Route path="/games/:gameCode" element={<GamePlay />} />
                </Routes>
            </MemoryRouter>,
        );

        await waitFor(() => {
            expect(screen.getByText(/Tu hipótesis ha sido refutada:/i)).toBeInTheDocument();
        });

        await act(async () => {
            vi.advanceTimersByTime(2600);
        });

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/turns/51/finish');
        });
        vi.useRealTimers();
    });
});

describe('GamePlay accusation', () => {
    const player = {
        id: 1,
        character: 'AMAPOLA',
        initialDiceResult: 6,
        eliminated: false,
        user: { username: 'player1' },
        currentBox: { id: 10, x: 10, y: 10, type: 'ROOM', room: 'CENTER' },
    };

    const startedState = {
        name: 'Test',
        code: 'TEST01',
        maxPlayers: 3,
        status: 'STARTED',
        players: [player],
        board: { boxes: [player.currentBox] },
        activeTurn: {
            id: 51,
            turnNumber: 1,
            status: 'ACTIVE',
            player,
            diceResult: null,
            hasMoved: false,
            expiresAt: null,
        },
        myHand: [],
        myBonusCards: [],
        faceUpCards: [],
        weaponTokens: [],
        solutionSaved: false,
        winner: null,
    };

    const renderGame = () => render(
        <MemoryRouter initialEntries={['/games/TEST01']}>
            <Routes>
                <Route path="/games/:gameCode" element={<GamePlay />} />
            </Routes>
        </MemoryRouter>,
    );

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('asks for confirmation before sending the accusation and shows a wrong result', async () => {
        api.get.mockResolvedValue({ data: startedState });
        api.post.mockResolvedValue({
            data: {
                correct: false,
                gameStatus: 'STARTED',
                winner: null,
                revealedHiddenCards: [{ character: 'RUBIO' }],
                solution: {
                    character: 'RUBIO',
                    weapon: 'CANDELABRO',
                    room: 'STUDY',
                },
            },
        });

        renderGame();

        const accuseButton = await screen.findByRole('button', { name: 'Formular acusación final' });
        expect(accuseButton).toBeDisabled();

        fireEvent.change(screen.getByLabelText('Seleccionar sospechoso de la acusación'), {
            target: { value: 'AMAPOLA' },
        });
        fireEvent.change(screen.getByLabelText('Seleccionar arma de la acusación'), {
            target: { value: 'PISTOLA' },
        });
        fireEvent.change(screen.getByLabelText('Seleccionar habitación de la acusación'), {
            target: { value: 'KITCHEN' },
        });
        expect(accuseButton).toBeEnabled();

        fireEvent.click(accuseButton);
        expect(screen.getByRole('dialog', { name: 'Confirmar acusación' })).toBeInTheDocument();
        expect(api.post).not.toHaveBeenCalled();

        fireEvent.click(screen.getByRole('button', { name: 'Corregir' }));
        expect(screen.queryByRole('dialog', { name: 'Confirmar acusación' })).not.toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Formular acusación final' }));
        fireEvent.click(screen.getByRole('button', { name: 'Confirmar acusación' }));

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/games/TEST01/accusations', {
                character: 'AMAPOLA',
                weapon: 'PISTOLA',
                room: 'KITCHEN',
            });
        });
        expect(await screen.findByRole('dialog', { name: 'Derrota' })).toBeInTheDocument();
        expect(screen.getByText(/tu acusación final era incorrecta/i)).toBeInTheDocument();
        const resultDialog = screen.getByRole('dialog', { name: 'Derrota' });
        expect(resultDialog.textContent.replace(/\s+/g, ' ')).toContain(
            'ha fallado la acusación y queda eliminado. No podrá volver a jugar.',
        );
        expect(resultDialog).toHaveTextContent('Candelabro');
        expect(resultDialog).toHaveTextContent('Estudio');
        expect(resultDialog.querySelector('.accusation-result-card img'))
            .toHaveAttribute('src', '/images/personajes/Rubio.png');
        expect(screen.getByRole('button', { name: 'Continuar viendo la partida' })).toBeInTheDocument();
    });

    it('announces the winner to a spectator without showing a victory screen', async () => {
        api.get.mockResolvedValue({
            data: {
                ...startedState,
                status: 'ENDED',
                activeTurn: null,
                winner: {
                    id: 2,
                    character: 'RUBIO',
                    eliminated: false,
                    user: { username: 'player2' },
                },
                solution: {
                    character: 'BLANCO',
                    weapon: 'CANDELABRO',
                    room: 'STUDY',
                },
            },
        });

        renderGame();

        expect(await screen.findByRole('dialog', { name: 'Partida finalizada' })).toBeInTheDocument();
        expect(screen.getByRole('dialog').textContent).toContain(
            'player2 (Rubio) ha ganado la partida. Tú estabas viendo la partida.',
        );
        expect(screen.getByText('Blanco')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Volver al menú' })).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Formular acusación final' })).not.toBeInTheDocument();
        expect(screen.queryByRole('heading', { name: '¡Victoria!' })).not.toBeInTheDocument();
    });

    it('shows a victory screen only to the player who won', async () => {
        api.get.mockResolvedValue({
            data: {
                ...startedState,
                status: 'ENDED',
                activeTurn: null,
                winner: {
                    ...player,
                    eliminated: false,
                },
                solution: {
                    character: 'AMAPOLA',
                    weapon: 'CANDELABRO',
                    room: 'STUDY',
                },
            },
        });

        renderGame();

        expect(await screen.findByRole('dialog', { name: '¡Victoria!' })).toBeInTheDocument();
        expect(screen.getByRole('dialog').textContent).toContain(
            'player1 (Amapola) ha ganado la partida al resolver el misterio.',
        );
    });

    it('shows no winner and the solution when every player has been eliminated', async () => {
        api.get.mockResolvedValue({
            data: {
                ...startedState,
                status: 'ENDED',
                players: [
                    { ...player, eliminated: true },
                    { id: 2, eliminated: true, character: 'RUBIO', user: { username: 'player2' } },
                ],
                activeTurn: null,
                winner: null,
                solution: {
                    character: 'BLANCO',
                    weapon: 'CANDELABRO',
                    room: 'STUDY',
                },
            },
        });

        renderGame();

        expect(await screen.findByRole('dialog', { name: 'Derrota' })).toBeInTheDocument();
        expect(screen.getByText(/nadie ha resuelto el misterio/i)).toBeInTheDocument();
        expect(screen.getByText(/no ha ganado nadie/i)).toBeInTheDocument();
        expect(screen.getByText('Blanco')).toBeInTheDocument();
        expect(screen.getByText('Candelabro')).toBeInTheDocument();
        expect(screen.getByText('Estudio')).toBeInTheDocument();
    });
});
