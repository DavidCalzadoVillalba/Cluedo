import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import GamePlayTopBar from './GamePlayTopBar';

describe('GamePlayTopBar', () => {
    const defaultProps = {
        gameName: 'Misterio en la Mansión',
        gameCode: 'ABC12',
        activePlayer: {
            user: { username: 'Detective' },
            character: 'AMAPOLA',
        },
        activeTurn: { turnNumber: 2 },
        isMyTurn: true,
        secondsLeft: 30,
        onExit: vi.fn(),
        onNotebookOpen: vi.fn(),
    };

    it('renders topbar info and top-left menu button', () => {
        render(<GamePlayTopBar {...defaultProps} />);

        expect(screen.getByRole('heading', { level: 1, name: 'Misterio en la Mansión' })).toBeInTheDocument();
        expect(screen.getByText('ABC12')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Abrir menú/i })).toBeInTheDocument();
    });

    it('opens the sidebar when clicking the top-left menu button', () => {
        render(<GamePlayTopBar {...defaultProps} />);

        const menuBtn = screen.getByRole('button', { name: /Abrir menú/i });
        fireEvent.click(menuBtn);

        expect(screen.getByRole('dialog', { name: /Menú de partida/i })).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 2, name: 'Menú' })).toBeInTheDocument();
        expect(screen.getByText(/PARTIDA EN CURSO/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Continuar partida/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Libreta de investigación/i })).toBeInTheDocument();
    });

    it('calls onExit when clicking "Salir al menú" inside the sidebar', () => {
        const onExitMock = vi.fn();
        render(<GamePlayTopBar {...defaultProps} onExit={onExitMock} />);

        // Open sidebar
        fireEvent.click(screen.getByRole('button', { name: /Abrir menú/i }));

        // Click exit in sidebar
        const exitButtons = screen.getAllByRole('button', { name: /Salir al menú/i });
        const sidebarExitBtn = exitButtons.find((btn) => btn.closest('.gameplay-sidebar'));
        expect(sidebarExitBtn).toBeDefined();
        fireEvent.click(sidebarExitBtn);

        expect(onExitMock).toHaveBeenCalledTimes(1);
        expect(screen.queryByRole('dialog', { name: /Menú de partida/i })).not.toBeInTheDocument();
    });

    it('closes the sidebar when clicking "Continuar partida" or the close button', () => {
        render(<GamePlayTopBar {...defaultProps} />);

        // Open sidebar
        fireEvent.click(screen.getByRole('button', { name: /Abrir menú/i }));
        expect(screen.getByRole('dialog', { name: /Menú de partida/i })).toBeInTheDocument();

        // Click close
        fireEvent.click(screen.getByRole('button', { name: 'Cerrar menú' }));
        expect(screen.queryByRole('dialog', { name: /Menú de partida/i })).not.toBeInTheDocument();

        // Open again and click continue
        fireEvent.click(screen.getByRole('button', { name: /Abrir menú/i }));
        fireEvent.click(screen.getByRole('button', { name: /Continuar partida/i }));
        expect(screen.queryByRole('dialog', { name: /Menú de partida/i })).not.toBeInTheDocument();
    });

    it('calls onNotebookOpen when clicking "Libreta de investigación" in sidebar', () => {
        const onNotebookOpenMock = vi.fn();
        render(<GamePlayTopBar {...defaultProps} onNotebookOpen={onNotebookOpenMock} />);

        fireEvent.click(screen.getByRole('button', { name: /Abrir menú/i }));
        fireEvent.click(screen.getByRole('button', { name: /Libreta de investigación/i }));

        expect(onNotebookOpenMock).toHaveBeenCalledTimes(1);
        expect(screen.queryByRole('dialog', { name: /Menú de partida/i })).not.toBeInTheDocument();
    });
});

