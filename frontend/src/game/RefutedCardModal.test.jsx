import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import RefutedCardModal from './RefutedCardModal';

describe('RefutedCardModal', () => {
    const baseSuggestion = {
        id: 1,
        character: 'AMAPOLA',
        weapon: 'PISTOLA',
        room: 'KITCHEN',
        refutedByPlayerName: 'Javi',
        shownCard: {
            id: 10,
            cardType: 'WEAPON',
            weapon: 'PISTOLA',
        },
    };

    it('renders nothing when isOpen is false', () => {
        const { container } = render(
            <RefutedCardModal
                isOpen={false}
                onClose={vi.fn()}
                refutedSuggestion={baseSuggestion}
            />,
        );
        expect(container.firstChild).toBeNull();
    });

    it('renders nothing when shownCard is missing', () => {
        const { container } = render(
            <RefutedCardModal
                isOpen={true}
                onClose={vi.fn()}
                refutedSuggestion={{ ...baseSuggestion, shownCard: null }}
            />,
        );
        expect(container.firstChild).toBeNull();
    });

    it('renders modal with title, refuter name, and visual card for a weapon', () => {
        render(
            <RefutedCardModal
                isOpen={true}
                onClose={vi.fn()}
                refutedSuggestion={baseSuggestion}
                onOpenNotebook={vi.fn()}
            />,
        );

        expect(screen.getByRole('dialog')).toBeInTheDocument();
        expect(screen.getByText(/HIPÓTESIS REFUTADA/i)).toBeInTheDocument();
        expect(screen.getByText(/Tu hipótesis ha sido refutada:/i)).toBeInTheDocument();
        expect(screen.getByText(/Javi/i)).toBeInTheDocument();
        expect(screen.getByText(/te ha mostrado la siguiente carta en secreto:/i)).toBeInTheDocument();

        // Card visual
        expect(screen.getByText('Arma')).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 3, name: 'Pistola' })).toBeInTheDocument();

        // Hypothesis context
        expect(screen.getByText(/Tu hipótesis incluía:/i)).toBeInTheDocument();
        expect(screen.getByText(/Amapola/i)).toBeInTheDocument();
        expect(screen.getByText(/Cocina/i)).toBeInTheDocument();

        // Buttons
        expect(screen.getByRole('button', { name: 'Entendido' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Abrir libreta/i })).toBeInTheDocument();
    });

    it('renders character card with character styling and artwork', () => {
        const charSuggestion = {
            id: 2,
            character: 'PRADO',
            weapon: 'CUCHILLO',
            room: 'PATIO',
            refutedByPlayerName: 'Pedro',
            shownCard: {
                id: 20,
                cardType: 'CHARACTER',
                character: 'PRADO',
            },
        };

        render(
            <RefutedCardModal
                isOpen={true}
                onClose={vi.fn()}
                refutedSuggestion={charSuggestion}
            />,
        );

        expect(screen.getByText('Sospechoso')).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 3, name: 'Prado' })).toBeInTheDocument();
        expect(screen.getByText(/Pedro/i)).toBeInTheDocument();
    });

    it('renders room card with room styling', () => {
        const roomSuggestion = {
            id: 3,
            character: 'BLANCO',
            weapon: 'CUERDA',
            room: 'LOUNGE',
            refutedByPlayerName: 'David',
            shownCard: {
                id: 30,
                cardType: 'ROOM',
                room: 'LOUNGE',
            },
        };

        render(
            <RefutedCardModal
                isOpen={true}
                onClose={vi.fn()}
                refutedSuggestion={roomSuggestion}
            />,
        );

        expect(screen.getByText('Habitación')).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 3, name: 'Salón' })).toBeInTheDocument();
        expect(screen.getByText(/David/i)).toBeInTheDocument();
    });

    it('handles legacy or mock card objects with name and type', () => {
        const mockSuggestion = {
            id: 4,
            character: 'AMAPOLA',
            weapon: 'PISTOLA',
            room: 'LOUNGE',
            refutedByPlayerName: 'player2',
            shownCard: { id: 10, name: 'PISTOLA', type: 'WEAPON' },
        };

        render(
            <RefutedCardModal
                isOpen={true}
                onClose={vi.fn()}
                refutedSuggestion={mockSuggestion}
            />,
        );

        expect(screen.getByText('Arma')).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 3, name: 'Pistola' })).toBeInTheDocument();
    });

    it('calls onClose when clicking the "Entendido" button, close (✕) button, or backdrop', () => {
        const onCloseMock = vi.fn();
        const { container } = render(
            <RefutedCardModal
                isOpen={true}
                onClose={onCloseMock}
                refutedSuggestion={baseSuggestion}
            />,
        );

        // Click "Entendido"
        fireEvent.click(screen.getByRole('button', { name: 'Entendido' }));
        expect(onCloseMock).toHaveBeenCalledTimes(1);

        // Click close icon button
        fireEvent.click(screen.getByRole('button', { name: 'Cerrar aviso de refutación' }));
        expect(onCloseMock).toHaveBeenCalledTimes(2);

        // Click backdrop overlay
        const overlay = container.querySelector('.refute-modal-overlay');
        fireEvent.click(overlay);
        expect(onCloseMock).toHaveBeenCalledTimes(3);
    });

    it('calls onOpenNotebook when clicking the "Abrir libreta" button', () => {
        const onOpenNotebookMock = vi.fn();
        render(
            <RefutedCardModal
                isOpen={true}
                onClose={vi.fn()}
                refutedSuggestion={baseSuggestion}
                onOpenNotebook={onOpenNotebookMock}
            />,
        );

        fireEvent.click(screen.getByRole('button', { name: /Abrir libreta/i }));
        expect(onOpenNotebookMock).toHaveBeenCalledTimes(1);
    });
});

