import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import InvestigationNotebook from './InvestigationNotebook';
import api from '../services/api';

vi.mock('../services/api', () => ({
    default: { get: vi.fn(), put: vi.fn() },
}));

describe('InvestigationNotebook', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        api.get.mockResolvedValue({
            data: [
                { id: 21, category: 'SUSPECT', character: 'PRADO', value: 'BLANK' },
                { id: 22, category: 'WEAPON', weapon: 'CANDELABRO', value: 'X' },
            ],
        });
        api.put.mockImplementation((_url, body) => Promise.resolve({ data: { value: body.value } }));
    });

    it('loads marks from the server and persists changes to the server', async () => {
        render(<InvestigationNotebook gameCode="TEST01" isOpen onClose={vi.fn()} />);

        expect(await screen.findByRole('button', { name: 'Prado: sin marcar. Cambiar marca' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Candelabro: descartado. Cambiar marca' })).toBeInTheDocument();
        expect(api.get).toHaveBeenCalledWith('/games/TEST01/notebook/marks');

        fireEvent.click(screen.getByRole('button', { name: 'Prado: sin marcar. Cambiar marca' }));
        fireEvent.click(screen.getByRole('button', { name: 'Marcar duda en Prado' }));

        await waitFor(() => {
            expect(api.put).toHaveBeenCalledWith('/games/TEST01/notebook/marks/21', { value: 'QUESTION' });
        });
        expect(await screen.findByRole('button', { name: 'Prado: duda. Cambiar marca' })).toBeInTheDocument();
    });

    it('reports server errors when marks cannot be loaded', async () => {
        api.get.mockRejectedValueOnce(new Error('offline'));

        render(<InvestigationNotebook gameCode="TEST01" isOpen onClose={vi.fn()} />);

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'No se han podido cargar las marcas de la libreta.',
        );
    });
});
