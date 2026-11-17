import { describe, expect, it, vi } from 'vitest';
import { render, screen, fireEvent, within } from '@testing-library/react';
import SuggestionPanel from './SuggestionPanel';

describe('SuggestionPanel Component (HU-016)', () => {
    const baseProps = {
        canSuggest: true,
        currentRoom: 'LOUNGE',
        suggestSuspect: '',
        suggestWeapon: '',
        isSuggesting: false,
        onSuspectChange: vi.fn(),
        onWeaponChange: vi.fn(),
        onSuggest: vi.fn(),
        hasSuggested: false,
        activeSuggestion: null,
        isHypothesisPending: false,
        isMyTurnToRefute: false,
        isMySuggestion: true,
        matchingCards: [],
        selectedRefuteCardId: null,
        isRefuting: false,
        onRefuteCardChange: vi.fn(),
        onRefute: vi.fn(),
        skipRefutationBonusCard: null,
        onPass: vi.fn(),
        myPlayerId: 1,
        activePlayerName: 'TestPlayer',
        usingBonusCardId: null,
        hasPeekCard: false,
        onPeek: vi.fn(),
    };

    it('renders nothing when cannot suggest and no active suggestion exists', () => {
        const { container } = render(
            <SuggestionPanel {...baseProps} canSuggest={false} />
        );
        expect(container).toBeEmptyDOMElement();
    });

    it('renders the centered suggestion modal when canSuggest is true', () => {
        render(<SuggestionPanel {...baseProps} />);

        expect(screen.getByRole('dialog', { name: 'Sugerencia' })).toBeInTheDocument();
        expect(screen.getByText('Salón')).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 3, name: 'Sospechoso' })).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 3, name: 'Arma' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Sugerir' })).toBeDisabled();
    });

    it('renders all 6 suspects and 6 weapons inside the modal', () => {
        render(<SuggestionPanel {...baseProps} />);

        const suspects = ['Amapola', 'Rubio', 'Blanco', 'Prado', 'Celeste', 'Mora'];
        suspects.forEach((name) => {
            expect(screen.getByRole('button', { name: `Sospechoso: ${name}` })).toBeInTheDocument();
        });

        const weapons = ['Candelabro', 'Cuchillo', 'Tubería de plomo', 'Pistola', 'Cuerda', 'Herramienta'];
        weapons.forEach((name) => {
            expect(screen.getByRole('button', { name: `Arma: ${name}` })).toBeInTheDocument();
        });
    });

    it('invokes onSuspectChange and onWeaponChange when selecting options', () => {
        render(<SuggestionPanel {...baseProps} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sospechoso: Prado' }));
        expect(baseProps.onSuspectChange).toHaveBeenCalledWith('PRADO');

        fireEvent.click(screen.getByRole('button', { name: 'Arma: Pistola' }));
        expect(baseProps.onWeaponChange).toHaveBeenCalledWith('PISTOLA');
    });

    it('enables the submit button when both suspect and weapon are selected and calls onSuggest', () => {
        render(
            <SuggestionPanel
                {...baseProps}
                suggestSuspect="PRADO"
                suggestWeapon="PISTOLA"
            />
        );

        const pradoButton = screen.getByRole('button', { name: 'Sospechoso: Prado' });
        expect(pradoButton).toHaveClass('selected');

        const pistolaButton = screen.getByRole('button', { name: 'Arma: Pistola' });
        expect(pistolaButton).toHaveClass('selected');

        const submitBtn = screen.getByRole('button', { name: 'Sugerir' });
        expect(submitBtn).not.toBeDisabled();

        fireEvent.click(submitBtn);
        expect(baseProps.onSuggest).toHaveBeenCalledTimes(1);
    });

    it('allows closing the modal to inspect board and reopening it from the action panel', () => {
        render(<SuggestionPanel {...baseProps} />);

        const closeBtn = screen.getByRole('button', { name: 'Cerrar sugerencia' });
        fireEvent.click(closeBtn);

        expect(screen.queryByRole('dialog', { name: 'Sugerencia' })).not.toBeInTheDocument();

        const reopenBtn = screen.getByRole('button', { name: /Formular hipótesis/i });
        expect(reopenBtn).toBeInTheDocument();

        fireEvent.click(reopenBtn);
        expect(screen.getByRole('dialog', { name: 'Sugerencia' })).toBeInTheDocument();
    });

    it('does not auto-open the modal when player has rolled dice and has to choose between suggesting or moving', () => {
        render(
            <SuggestionPanel
                {...baseProps}
                canSuggest={true}
                diceResult={5}
                hasMoved={false}
            />
        );

        // Modal is NOT opened covering the board
        expect(screen.queryByRole('dialog', { name: 'Sugerencia' })).not.toBeInTheDocument();

        // Choice prompt is shown
        expect(screen.getByText('¿Qué deseas hacer en tu turno?')).toBeInTheDocument();
        expect(screen.getByText(/Tirada:/i)).toBeInTheDocument();
        expect(screen.getByText(/O pulsa una casilla iluminada en el tablero para moverte/i)).toBeInTheDocument();

        // Clicking the button opens the suggestion modal
        const suggestBtn = screen.getByRole('button', { name: /Formular hipótesis en Salón/i });
        fireEvent.click(suggestBtn);
        expect(screen.getByRole('dialog', { name: 'Sugerencia' })).toBeInTheDocument();
    });
});

describe('SuggestionPanel Component (HU-017 Refutation)', () => {
    const activeSuggestion = {
        id: 10,
        character: 'PRADO',
        weapon: 'PISTOLA',
        room: 'LOUNGE',
        suggestedByPlayerName: 'Javi',
        suggestedByPlayerId: 2,
        status: 'PENDING',
    };

    const myMatchingCard = {
        id: 101,
        cardType: 'CHARACTER',
        character: 'PRADO',
    };

    const myNonMatchingCard = {
        id: 102,
        cardType: 'ROOM',
        room: 'KITCHEN',
    };

    const mySecondMatchingCard = {
        id: 103,
        cardType: 'WEAPON',
        weapon: 'PISTOLA',
    };

    const refuteBaseProps = {
        canSuggest: false,
        currentRoom: 'KITCHEN',
        suggestSuspect: '',
        suggestWeapon: '',
        isSuggesting: false,
        onSuspectChange: vi.fn(),
        onWeaponChange: vi.fn(),
        onSuggest: vi.fn(),
        hasSuggested: true,
        activeSuggestion,
        isHypothesisPending: true,
        isMyTurnToRefute: true,
        isMySuggestion: false,
        matchingCards: [myMatchingCard],
        selectedRefuteCardId: null,
        isRefuting: false,
        onRefuteCardChange: vi.fn(),
        onRefute: vi.fn(),
        skipRefutationBonusCard: null,
        onPass: vi.fn(),
        myPlayerId: 1,
        activePlayerName: 'Javi',
        usingBonusCardId: null,
        hasPeekCard: false,
        onPeek: vi.fn(),
        myHand: [myMatchingCard, myNonMatchingCard],
        players: [
            { id: 2, user: { username: 'Javi' }, character: 'RUBIO' },
            { id: 1, user: { username: 'Pedro' }, character: 'BLANCO' },
        ],
    };

    it('renders the centered refutation modal with suggester name and the 3 suggested cards', () => {
        render(<SuggestionPanel {...refuteBaseProps} />);

        expect(screen.getByRole('dialog')).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 2, name: /Javi ha hecho una sugerencia/i })).toBeInTheDocument();
        expect(screen.getByText(/Ahora es tu turno de refutar la teoría/i)).toBeInTheDocument();

        // 3 suggested cards categories and names inside the suggested region
        const suggestedRegion = screen.getByRole('region', { name: 'Elementos de la sugerencia' });
        expect(within(suggestedRegion).getByText('Sospechoso')).toBeInTheDocument();
        expect(within(suggestedRegion).getByText('Prado')).toBeInTheDocument();

        expect(within(suggestedRegion).getByText('Arma')).toBeInTheDocument();
        expect(within(suggestedRegion).getByText('Pistola')).toBeInTheDocument();

        expect(within(suggestedRegion).getByText('Habitación')).toBeInTheDocument();
        expect(within(suggestedRegion).getByText('Salón')).toBeInTheDocument();
    });

    it('renders player hand cards, highlighting matching cards and disabling non-matching cards', () => {
        render(<SuggestionPanel {...refuteBaseProps} />);

        // Matching card is enabled and selected (since it is the only matching card)
        const matchingBtn = screen.getByRole('button', { name: /Refutar con Sospechoso: Prado/i });
        expect(matchingBtn).toBeInTheDocument();
        expect(matchingBtn).toHaveClass('is-match');
        expect(matchingBtn).toHaveClass('is-selected');

        // Non matching card is rendered disabled
        const nonMatchingCard = screen.getByTitle(/Cocina \(no coincide con la sugerencia\)/i);
        expect(nonMatchingCard).toBeInTheDocument();
        expect(nonMatchingCard).toHaveClass('is-disabled');

        // Submit button is enabled
        const submitBtn = screen.getByRole('button', { name: 'Mostrar carta' });
        expect(submitBtn).not.toBeDisabled();

        fireEvent.click(submitBtn);
        expect(refuteBaseProps.onRefute).toHaveBeenCalledTimes(1);
    });

    it('allows choosing between multiple matching cards in hand', () => {
        const onRefuteCardChangeMock = vi.fn();
        render(
            <SuggestionPanel
                {...refuteBaseProps}
                matchingCards={[myMatchingCard, mySecondMatchingCard]}
                myHand={[myMatchingCard, mySecondMatchingCard, myNonMatchingCard]}
                onRefuteCardChange={onRefuteCardChangeMock}
                selectedRefuteCardId={103}
            />
        );

        const card1 = screen.getByRole('button', { name: /Refutar con Sospechoso: Prado/i });
        const card2 = screen.getByRole('button', { name: /Refutar con Arma: Pistola/i });

        expect(card1).toHaveClass('is-match');
        expect(card2).toHaveClass('is-match');
        expect(card2).toHaveClass('is-selected');

        fireEvent.click(card1);
        expect(onRefuteCardChangeMock).toHaveBeenCalledWith(101);
    });

    it('renders "No puedo refutar (Pasar)" when player has no matching cards', () => {
        const onPassMock = vi.fn();
        render(
            <SuggestionPanel
                {...refuteBaseProps}
                matchingCards={[]}
                myHand={[myNonMatchingCard]}
                onPass={onPassMock}
            />
        );

        expect(screen.getByText(/No tienes ninguna carta que coincida con la sugerencia/i)).toBeInTheDocument();

        const passBtn = screen.getByRole('button', { name: 'No puedo refutar (Pasar)' });
        expect(passBtn).toBeInTheDocument();

        fireEvent.click(passBtn);
        expect(onPassMock).toHaveBeenCalledWith(false);
    });

    it('renders "Usar carta bonus (Pasar)" when player has a skip refutation bonus card', () => {
        const onPassMock = vi.fn();
        render(
            <SuggestionPanel
                {...refuteBaseProps}
                skipRefutationBonusCard={{ id: 99, effect: 'SKIP_REFUTATION' }}
                onPass={onPassMock}
            />
        );

        const bonusPassBtn = screen.getByRole('button', { name: /Usar carta bonus \(Pasar\)/i });
        expect(bonusPassBtn).toBeInTheDocument();

        fireEvent.click(bonusPassBtn);
        expect(onPassMock).toHaveBeenCalledWith(true);
    });

    it('allows closing the refutation modal to inspect the board and reopening it', () => {
        render(<SuggestionPanel {...refuteBaseProps} />);

        const closeBtn = screen.getByRole('button', { name: 'Cerrar refutación' });
        fireEvent.click(closeBtn);

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

        const reopenBtn = screen.getByRole('button', { name: /Abrir ventana de refutación/i });
        expect(reopenBtn).toBeInTheDocument();

        fireEvent.click(reopenBtn);
        expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
});
