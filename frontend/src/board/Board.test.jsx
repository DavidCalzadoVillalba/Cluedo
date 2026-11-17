import { describe, test, expect, vi } from 'vitest';
import Board from './Board';
import { CELL_TEXTURES, assignTexture, assignRange } from './boardTextures';
import { render, screen, fireEvent } from '../test-utils';

describe('Board Component', () => {
    test('renders 399 square buttons for a default 21x19 board', () => {
        render(<Board />);
        const buttons = screen.getAllByRole('button');
        // Total buttons: 399 cell buttons + header/footer control buttons
        const cellButtons = buttons.filter((btn) => btn.id && btn.id.startsWith('cell-'));
        expect(cellButtons).toHaveLength(399);
    });

    test('each cell button has assigned cell id and coordinate data attributes', () => {
        render(<Board />);

        // First cell (0, 0)
        const firstCell = document.getElementById('cell-1');
        expect(firstCell).toBeInTheDocument();
        expect(firstCell).toHaveAttribute('data-cell-id', '1');
        expect(firstCell).toHaveAttribute('data-x', '0');
        expect(firstCell).toHaveAttribute('data-y', '0');

        // Last cell (20, 18) -> 19 * 21 = 399
        const lastCell = document.getElementById('cell-399');
        expect(lastCell).toBeInTheDocument();
        expect(lastCell).toHaveAttribute('data-cell-id', '399');
        expect(lastCell).toHaveAttribute('data-x', '20');
        expect(lastCell).toHaveAttribute('data-y', '18');
    });

    test('clicking a cell invokes onCellClick callback and updates selected display', () => {
        const handleClick = vi.fn();
        render(<Board onCellClick={handleClick} />);

        const targetCell = document.getElementById('cell-200');
        fireEvent.click(targetCell);

        expect(handleClick).toHaveBeenCalledTimes(1);
        expect(handleClick).toHaveBeenCalledWith(
            expect.objectContaining({
                id: 200,
            })
        );

        // Verification of status display update
        const statusElement = screen.getByRole('status');
        expect(statusElement).toHaveTextContent('ID #200');
    });

    test('renders all players and weapons sharing a cell', () => {
        render(
            <Board
                players={[
                    { id: 1, character: 'AMAPOLA', currentBox: { x: 2, y: 1 } },
                    { id: 2, character: 'RUBIO', currentBox: { x: 2, y: 1 } },
                ]}
                weapons={[
                    { id: 3, weapon: 'CUERDA', currentBox: { x: 2, y: 1 } },
                ]}
            />
        );

        const cell = document.getElementById('cell-24');
        expect(cell).toHaveAttribute('data-x', '2');
        expect(cell).toHaveAttribute('data-y', '1');
        expect(cell.querySelectorAll('.cell-piece')).toHaveLength(3);
        expect(cell).toHaveAccessibleName('Casilla 24, columna 2, fila 1');
        expect(cell.querySelector('[aria-label="Jugador: Amapola"]')).toBeInTheDocument();
        expect(cell.querySelector('[aria-label="Jugador: Rubio"]')).toBeInTheDocument();
        expect(cell.querySelector('[aria-label="Arma: Cuerda"]')).toBeInTheDocument();
        expect(cell.querySelector('[aria-label="Arma: Cuerda"] img')).toHaveAttribute(
            'src',
            '/images/armas/cuerda-3d.png'
        );
        expect(cell.querySelector('[aria-label="Jugador: Amapola"] img')).toHaveAttribute(
            'src',
            '/images/fichas/amapola-3d.png'
        );
        expect(document.getElementById('cell-45').querySelector('.cell-pieces')).toBeNull();
    });

    test('renders 3D weapon token for PISTOLA', () => {
        render(
            <Board
                weapons={[
                    { id: 4, weapon: 'PISTOLA', currentBox: { x: 3, y: 1 } },
                ]}
            />
        );

        const cell = document.getElementById('cell-25');
        expect(cell.querySelector('[aria-label="Arma: Pistola"]')).toBeInTheDocument();
        expect(cell.querySelector('.cell-piece-weapon-3d')).toBeInTheDocument();
        expect(cell.querySelector('[aria-label="Arma: Pistola"] img')).toHaveAttribute(
            'src',
            '/images/armas/pistola-3d.png'
        );
    });

    test('shows each player character color in the player slots', () => {
        render(
            <Board
                players={[
                    {
                        id: 1,
                        character: 'AMAPOLA',
                        user: { username: 'Detective' },
                    },
                ]}
            />
        );

        const playerIcon = document.querySelector('.player-avatar-circle');
        expect(playerIcon).toHaveClass('color-amapola');
        expect(playerIcon.querySelector('img')).toBeNull();
    });

    test('uses the visual cell position for textures and labels, not database IDs', () => {
        render(
            <Board
                boxes={[
                    { id: 901, x: 0, y: 0, type: 'PATH' },
                    { id: 902, x: 1, y: 0, type: 'PATH' },
                ]}
                initialShowIds
            />
        );

        const firstCell = document.getElementById('cell-901');
        const secondCell = document.getElementById('cell-902');
        expect(firstCell).toHaveAttribute('data-x', '0');
        expect(firstCell).toHaveAttribute('data-y', '0');
        expect(firstCell.querySelector('.cell-id-label')).toHaveTextContent('1');
        expect(firstCell).toHaveStyle({ backgroundImage: 'url(/images/textures/FINAL_2_50x59.png)' });
        expect(secondCell.querySelector('.cell-id-label')).toHaveTextContent('2');
        expect(secondCell).toHaveStyle({ backgroundImage: 'url(/images/textures/FINAL_22_55x59.png)' });
    });

    test('keeps the static board room mapping instead of backend box labels', () => {
        render(
            <Board
                boxes={[
                    { id: 901, x: 2, y: 0, type: 'ROOM', room: 'KITCHEN' },
                    { id: 902, x: 20, y: 19, type: 'PATH' },
                ]}
                cols={21}
                rows={19}
            />
        );

        const bedroomCell = document.getElementById('cell-901');
        expect(bedroomCell).toHaveAttribute('data-group', 'BEDROOM');
        expect(bedroomCell).not.toHaveClass('cell-room');
        expect(document.querySelectorAll('[id^="cell-"]')).toHaveLength(399);
    });

    test('highlights backend-reachable cells without changing the visual room map', () => {
        render(
            <Board
                boxes={[
                    { id: 901, x: 2, y: 0, type: 'PATH', room: null },
                    { id: 902, x: 3, y: 0, type: 'BONUS', room: null },
                ]}
                reachableBoxIds={[902]}
            />
        );

        expect(document.getElementById('cell-902')).toHaveClass('is-reachable');
        expect(document.getElementById('cell-901')).not.toHaveClass('is-reachable');
        expect(document.getElementById('cell-902')).toHaveAttribute('data-group', 'BEDROOM');
    });

    test.each([
        { id: 381, x: 12, y: 8 },
        { id: 506, x: 11, y: 14 },
        { id: 462, x: 9, y: 12 },
        { id: 355, x: 7, y: 7 },
        { id: 316, x: 10, y: 5 },
    ])('shows a reachable bonus cell with database ID $id at ($x, $y)', ({ id, x, y }) => {
        render(
            <Board
                boxes={[{ id, x, y, type: 'BONUS', room: null }]}
                reachableBoxIds={[id]}
            />
        );

        const bonusCell = document.getElementById(`cell-${id}`);
        expect(bonusCell).toHaveClass('is-reachable');
        expect(bonusCell.querySelector('.reachable-step-indicator')).toBeInTheDocument();

        fireEvent.click(bonusCell);
        expect(bonusCell).toHaveClass('selected');
    });

    test('shows question marks only on the configured bonus cells', () => {
        expect(CELL_TEXTURES[116]).toBe('/images/textures/FINAL_207_51x48.png');
        expect(CELL_TEXTURES[155]).toBe('/images/textures/FINAL_149_47x48.png');
        expect(CELL_TEXTURES[262]).toBe('/images/textures/FINAL_194_47x52.png');
        expect(CELL_TEXTURES[306]).toBe('/images/textures/FINAL_236_55x50.png');
        expect(CELL_TEXTURES[181]).toBe('/images/textures/FINAL_250_44x45_bonus.png');
        expect(CELL_TEXTURES[93]).toBe('/images/textures/FINAL_166_51x48_no_bonus.png');
        expect(CELL_TEXTURES[244]).toBe('/images/textures/FINAL_250_44x45.png');
    });

    test('highlights every cell in a room while hovering one of its cells', () => {
        render(<Board />);

        const bedroomCell = document.getElementById('cell-3');
        const anotherBedroomCell = document.getElementById('cell-24');
        const bathroomCell = document.getElementById('cell-7');

        fireEvent.mouseEnter(bedroomCell);

        expect(bedroomCell).toHaveClass('group-hovered');
        expect(anotherBedroomCell).toHaveClass('group-hovered');
        expect(bathroomCell).not.toHaveClass('group-hovered');

        fireEvent.mouseLeave(bedroomCell);

        expect(bedroomCell).not.toHaveClass('group-hovered');
        expect(anotherBedroomCell).not.toHaveClass('group-hovered');
    });

    test('toggles cell ID visibility when clicking "Ver IDs" button', () => {
        render(<Board />);
        const toggleBtn = screen.getByRole('button', { name: /Ver IDs/i });

        expect(screen.queryByText('1')).not.toBeInTheDocument();

        fireEvent.click(toggleBtn);
        expect(screen.getByText('1')).toBeInTheDocument();
        expect(screen.getByText('399')).toBeInTheDocument();
    });

    test('board grid has a default width of 600px and accepts custom width', () => {
        const { rerender } = render(<Board />);
        const grid = screen.getByRole('grid');
        expect(grid).toHaveStyle({ width: '600px' });

        rerender(<Board cols={19} rows={20} width={900} />);
        expect(grid).toHaveStyle({ width: '900px' });
    });

    test('assigns individual textures to cells by ID', () => {
        const textures = {
            1: '/images/textures/puerta1.png',
            200: '/images/textures/baldosa.png',
        };

        render(<Board cellTextures={textures} />);

        const cell1 = document.getElementById('cell-1');
        const cell42 = document.getElementById('cell-200');
        const cell2 = document.getElementById('cell-2');

        expect(cell1).toHaveClass('has-texture');
        expect(cell1).toHaveStyle({ backgroundImage: 'url(/images/textures/puerta1.png)' });

        expect(cell42).toHaveClass('has-texture');
        expect(cell42).toHaveStyle({ backgroundImage: 'url(/images/textures/baldosa.png)' });

        expect(cell2).not.toHaveClass('has-texture');
        expect(cell2.style.backgroundImage).toBe('');
    });

    test('assigns textures to a list of cell IDs and ranges', () => {
        const textures = {
            // Lista mediante función auxiliar
            ...assignTexture([10, 11, 12], '/images/textures/baldosa_madera.png'),
            // Rango mediante función auxiliar
            ...assignRange(20, 22, '/images/textures/alfombra.png'),
            // Lista separada por comas
            '30, 31': '/images/textures/piedra.png',
        };

        render(<Board cellTextures={textures} />);

        // Verificar lista
        [10, 11, 12].forEach((id) => {
            const cell = document.getElementById(`cell-${id}`);
            expect(cell).toHaveClass('has-texture');
            expect(cell).toHaveStyle({ backgroundImage: 'url(/images/textures/baldosa_madera.png)' });
        });

        // Verificar rango
        [20, 21, 22].forEach((id) => {
            const cell = document.getElementById(`cell-${id}`);
            expect(cell).toHaveClass('has-texture');
            expect(cell).toHaveStyle({ backgroundImage: 'url(/images/textures/alfombra.png)' });
        });

        // Verificar string con comas
        [30, 31].forEach((id) => {
            const cell = document.getElementById(`cell-${id}`);
            expect(cell).toHaveClass('has-texture');
            expect(cell).toHaveStyle({ backgroundImage: 'url(/images/textures/piedra.png)' });
        });
    });

    test('renders dice action button with text and image for current user during initial roll', () => {
        const handleInitialRoll = vi.fn();
        render(
            <Board
                players={[
                    {
                        id: 1,
                        character: 'AMAPOLA',
                        user: { username: 'PlayerOne' },
                        initialDiceResult: null,
                    },
                    {
                        id: 2,
                        character: 'RUBIO',
                        user: { username: 'PlayerTwo' },
                        initialDiceResult: null,
                    },
                ]}
                currentUsername="PlayerOne"
                onInitialRoll={handleInitialRoll}
            />
        );

        const rollButton = screen.getByRole('button', { name: /pulse para tirar los dados/i });
        expect(rollButton).toBeInTheDocument();
        const diceImg = rollButton.querySelector('img');
        expect(diceImg).toHaveAttribute('src', '/images/dice.svg');

        fireEvent.click(rollButton);
        expect(handleInitialRoll).toHaveBeenCalledTimes(1);

        // PlayerTwo should be in waiting state
        expect(screen.getByText(/esperando tirada\.\.\./i)).toBeInTheDocument();
    });

    test('renders dice faces and total score when a player has rolled', () => {
        render(
            <Board
                players={[
                    {
                        id: 1,
                        character: 'AMAPOLA',
                        user: { username: 'PlayerOne' },
                        initialDiceOne: 4,
                        initialDiceTwo: 5,
                        initialDiceResult: 9,
                    },
                    {
                        id: 2,
                        character: 'RUBIO',
                        user: { username: 'PlayerTwo' },
                        initialDiceResult: null,
                    },
                ]}
                currentUsername="PlayerTwo"
            />
        );

        expect(screen.getByText('Total: 9')).toBeInTheDocument();
        expect(screen.getByText('4')).toBeInTheDocument();
        expect(screen.getByText('5')).toBeInTheDocument();
    });

    test('renders dice action button for active player during normal turn when not rolled yet', () => {
        const handleRollTurn = vi.fn();
        render(
            <Board
                players={[
                    {
                        id: 1,
                        character: 'AMAPOLA',
                        user: { username: 'PlayerOne' },
                        initialDiceResult: 5,
                    },
                    {
                        id: 2,
                        character: 'RUBIO',
                        user: { username: 'PlayerTwo' },
                        initialDiceResult: 4,
                    },
                ]}
                activeTurn={{
                    id: 10,
                    player: { id: 1 },
                    diceResult: null,
                    diceOne: null,
                    diceTwo: null,
                    hasMoved: false,
                }}
                currentUsername="PlayerOne"
                onRollTurn={handleRollTurn}
            />
        );

        const rollButton = screen.getByRole('button', { name: /pulse para tirar los dados/i });
        expect(rollButton).toBeInTheDocument();
        fireEvent.click(rollButton);
        expect(handleRollTurn).toHaveBeenCalledTimes(1);
    });

    test('renders waiting state for other players active turn when not rolled yet', () => {
        render(
            <Board
                players={[
                    {
                        id: 1,
                        character: 'AMAPOLA',
                        user: { username: 'PlayerOne' },
                        initialDiceResult: 5,
                    },
                    {
                        id: 2,
                        character: 'RUBIO',
                        user: { username: 'PlayerTwo' },
                        initialDiceResult: 4,
                    },
                ]}
                activeTurn={{
                    id: 10,
                    player: { id: 2 },
                    diceResult: null,
                    diceOne: null,
                    diceTwo: null,
                    hasMoved: false,
                }}
                currentUsername="PlayerOne"
            />
        );

        expect(screen.getByText(/esperando tirada\.\.\./i)).toBeInTheDocument();
    });

    test('renders dice result and total for active player during turn when rolled', () => {
        render(
            <Board
                players={[
                    {
                        id: 1,
                        character: 'AMAPOLA',
                        user: { username: 'PlayerOne' },
                        initialDiceResult: 5,
                    },
                    {
                        id: 2,
                        character: 'RUBIO',
                        user: { username: 'PlayerTwo' },
                        initialDiceResult: 4,
                    },
                ]}
                activeTurn={{
                    id: 10,
                    player: { id: 1 },
                    diceResult: 7,
                    diceOne: 3,
                    diceTwo: 4,
                    hasMoved: false,
                }}
                currentUsername="PlayerOne"
            />
        );

        expect(screen.getByText('Total: 7')).toBeInTheDocument();
        expect(screen.getByText('3')).toBeInTheDocument();
        expect(screen.getByText('4')).toBeInTheDocument();
    });

    test('positions 4 players in the 4 corner slots of the board', () => {
        render(
            <Board
                players={[
                    { id: 1, character: 'AMAPOLA', user: { username: 'P1' } },
                    { id: 2, character: 'RUBIO', user: { username: 'P2' } },
                    { id: 3, character: 'BLANCO', user: { username: 'P3' } },
                    { id: 4, character: 'PRADO', user: { username: 'P4' } },
                ]}
            />
        );

        expect(document.querySelector('.player-slot-one')).toBeInTheDocument();
        expect(document.querySelector('.player-slot-two')).toBeInTheDocument();
        expect(document.querySelector('.player-slot-four')).toBeInTheDocument();
        expect(document.querySelector('.player-slot-five')).toBeInTheDocument();
        expect(document.querySelector('.player-slot-three')).toBeNull();
        expect(document.querySelector('.player-slot-six')).toBeNull();
    });

    test('renders turn countdown timer on active player slot', () => {
        render(
            <Board
                players={[
                    { id: 1, character: 'AMAPOLA', user: { username: 'P1' } },
                    { id: 2, character: 'RUBIO', user: { username: 'P2' } },
                ]}
                activeTurn={{
                    id: 10,
                    turnNumber: 1,
                    player: { id: 1, character: 'AMAPOLA', user: { username: 'P1' } },
                }}
                secondsLeft={45}
            />
        );

        expect(screen.getByText(/45\s*s/)).toBeInTheDocument();
        expect(document.querySelector('.player-slot-timer')).toBeInTheDocument();
        const ringSvg = document.querySelector('.player-avatar-ring-svg');
        expect(ringSvg).toBeInTheDocument();
        expect(ringSvg).not.toHaveClass('is-warning');
    });

    test('turns avatar timer ring red with warning class when less than 10 seconds remain', () => {
        render(
            <Board
                players={[
                    { id: 1, character: 'AMAPOLA', user: { username: 'P1' } },
                    { id: 2, character: 'RUBIO', user: { username: 'P2' } },
                ]}
                activeTurn={{
                    id: 10,
                    turnNumber: 1,
                    player: { id: 1, character: 'AMAPOLA', user: { username: 'P1' } },
                }}
                secondsLeft={8}
            />
        );

        const ringSvg = document.querySelector('.player-avatar-ring-svg');
        expect(ringSvg).toBeInTheDocument();
        expect(ringSvg).toHaveClass('is-warning');
        expect(document.querySelector('.player-slot-placeholder.is-active')).toHaveClass('is-warning');
        expect(document.querySelector('.player-slot-two')).toHaveClass('is-dimmed');
    });

    test('renders character pieces superposed with full visibility and active player highlighted', () => {
        render(
            <Board
                boxes={[{ id: 1, x: 0, y: 0, row: 0, col: 0, type: 'HALLWAY' }]}
                players={[
                    { id: 1, character: 'AMAPOLA', user: { username: 'P1' }, currentBox: { id: 1 } },
                    { id: 2, character: 'RUBIO', user: { username: 'P2' }, currentBox: { id: 1 } },
                ]}
                activeTurn={{
                    id: 10,
                    turnNumber: 1,
                    player: { id: 1, character: 'AMAPOLA', user: { username: 'P1' } },
                }}
                secondsLeft={40}
            />
        );

        const cell = document.querySelector('#cell-1');
        expect(cell).toHaveClass('has-players');
        expect(cell).toHaveClass('has-active-player');

        const pieces = document.querySelectorAll('.cell-piece-player');
        expect(pieces).toHaveLength(2);
        pieces.forEach((p) => {
            expect(p).not.toHaveClass('is-dimmed');
        });

        const activePiece = document.querySelector('.cell-piece-player.is-active-turn');
        expect(activePiece).toBeInTheDocument();
        expect(activePiece).toHaveClass('color-amapola');
    });

    test('renders hand of cards at the bottom of the board as in HU-013 mockup', () => {
        render(
            <Board
                myHand={[
                    { id: 1, cardType: 'CHARACTER', character: 'MORA' },
                    { id: 2, cardType: 'ROOM', room: 'BEDROOM' },
                    { id: 3, cardType: 'WEAPON', weapon: 'PISTOLA' },
                ]}
            />
        );

        const handContainer = screen.getByLabelText('Cartas en mi mano');
        expect(handContainer).toBeInTheDocument();
        expect(screen.getByLabelText('Carta: Mora, Sospechoso')).toBeInTheDocument();
        expect(screen.getByLabelText('Carta: Dormitorio, Habitación')).toBeInTheDocument();
        expect(screen.getByLabelText('Carta: Pistola, Arma')).toBeInTheDocument();
        expect(document.querySelectorAll('.board-hand-card')).toHaveLength(3);
        expect(document.querySelector('.board-hand-counter')).not.toBeInTheDocument();
        expect(screen.queryByTitle(/total de cartas/i)).not.toBeInTheDocument();
    });

    test('renders finish turn button below timer for active user when dice have been rolled', () => {
        const onFinishTurnMock = vi.fn();
        render(
            <Board
                players={[
                    { id: 1, character: 'AMAPOLA', user: { username: 'currentUser' } },
                    { id: 2, character: 'RUBIO', user: { username: 'P2' } },
                ]}
                currentUsername="currentUser"
                activeTurn={{
                    id: 10,
                    turnNumber: 1,
                    player: { id: 1, character: 'AMAPOLA', user: { username: 'currentUser' } },
                    diceResult: 5,
                    hasMoved: true,
                }}
                secondsLeft={35}
                onFinishTurn={onFinishTurnMock}
            />
        );

        const timer = screen.getByTitle('Tiempo restante del turno');
        expect(timer).toBeInTheDocument();
        expect(timer).toHaveTextContent('35s');

        const finishBtn = screen.getByRole('button', { name: 'Finalizar turno' });
        expect(finishBtn).toBeInTheDocument();
        expect(finishBtn).toHaveClass('player-slot-finish-turn-btn');

        fireEvent.click(finishBtn);
        expect(onFinishTurnMock).toHaveBeenCalledTimes(1);
    });

    test('disables player slot finish turn button when finishing or hypothesis pending', () => {
        render(
            <Board
                players={[
                    { id: 1, character: 'AMAPOLA', user: { username: 'currentUser' } },
                ]}
                currentUsername="currentUser"
                activeTurn={{
                    id: 10,
                    turnNumber: 1,
                    player: { id: 1, character: 'AMAPOLA', user: { username: 'currentUser' } },
                    diceResult: 4,
                }}
                secondsLeft={20}
                onFinishTurn={vi.fn()}
                isFinishingTurn={true}
                isHypothesisPending={true}
            />
        );

        const finishBtn = screen.getByRole('button', { name: 'Finalizando...' });
        expect(finishBtn).toBeDisabled();
    });

    test('opens menu sidebar when clicking top-left menu button and calls onExitMenu', () => {
        const onExitMenuMock = vi.fn();
        render(<Board onExitMenu={onExitMenuMock} />);

        const menuBtn = screen.getByRole('button', { name: /menú/i });
        expect(menuBtn).toBeInTheDocument();

        expect(screen.queryByRole('complementary', { name: 'Menú del juego' })).not.toBeInTheDocument();

        fireEvent.click(menuBtn);

        const sidebar = screen.getByRole('complementary', { name: 'Menú del juego' });
        expect(sidebar).toBeInTheDocument();

        const exitBtn = screen.getByRole('button', { name: /salir al menú/i });
        expect(exitBtn).toBeInTheDocument();

        fireEvent.click(exitBtn);
        expect(onExitMenuMock).toHaveBeenCalledTimes(1);
        expect(screen.queryByRole('complementary', { name: 'Menú del juego' })).not.toBeInTheDocument();
    });

    test('closes menu sidebar when clicking close button or pressing Escape', () => {
        render(<Board />);

        const menuBtn = screen.getByRole('button', { name: /menú/i });
        fireEvent.click(menuBtn);

        expect(screen.getByRole('complementary', { name: 'Menú del juego' })).toBeInTheDocument();

        const closeBtn = screen.getByRole('button', { name: 'Cerrar menú' });
        fireEvent.click(closeBtn);

        expect(screen.queryByRole('complementary', { name: 'Menú del juego' })).not.toBeInTheDocument();

        fireEvent.click(menuBtn);
        expect(screen.getByRole('complementary', { name: 'Menú del juego' })).toBeInTheDocument();

        fireEvent.keyDown(window, { key: 'Escape' });
        expect(screen.queryByRole('complementary', { name: 'Menú del juego' })).not.toBeInTheDocument();
    });

    test('renders game room code next to Mansión text when gameCode is provided', () => {
        const { rerender } = render(<Board gameCode="ABCD12" />);

        expect(screen.getByText('Mansión')).toBeInTheDocument();
        expect(screen.getByText('ABCD12')).toBeInTheDocument();
        expect(screen.getByTitle('Código de la sala')).toBeInTheDocument();

        rerender(<Board gameCode={null} />);
        expect(screen.queryByTitle('Código de la sala')).not.toBeInTheDocument();
    });

    test('renders secret passage button when canUsePassage is true and calls onSecretPassage on click', () => {
        const onSecretPassageMock = vi.fn();
        const activePlayer = {
            id: 1,
            user: { username: 'testuser' },
            character: 'AMAPOLA',
            turnOrder: 1,
        };
        const activeTurn = {
            id: 10,
            status: 'ACTIVE',
            player: activePlayer,
            diceResult: null,
            hasMoved: false,
        };

        render(
            <Board
                players={[activePlayer]}
                activeTurn={activeTurn}
                currentUsername="testuser"
                canUsePassage={true}
                currentRoom="KITCHEN"
                passageDestinationRoom="GARAGE"
                onSecretPassage={onSecretPassageMock}
            />
        );

        const topPassageButton = screen.getByRole('button', {
            name: /entrar por el pasadizo a garaje/i,
        });
        expect(topPassageButton).toBeInTheDocument();

        fireEvent.click(topPassageButton);
        expect(onSecretPassageMock).toHaveBeenCalledTimes(1);
    });
});
