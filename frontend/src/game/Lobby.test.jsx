import { describe, expect, test, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import api from '../services/api';
import Lobby from './Lobby';

vi.mock('../services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        delete: vi.fn(),
    },
}));

describe('Lobby', () => {
    test('redirects every player when the game status becomes started', async () => {
        api.get.mockResolvedValue({ data: { status: 'started' } });

        render(
            <MemoryRouter initialEntries={['/game/lobby/ABC123']}>
                <Routes>
                    <Route path="/game/lobby/:gameCode" element={<Lobby />} />
                    <Route path="/game/play/:gameCode" element={<p>Partida abierta</p>} />
                </Routes>
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByText('Partida abierta')).toBeInTheDocument();
        });
        expect(api.get).toHaveBeenCalledWith('/games/ABC123');
    });
});
