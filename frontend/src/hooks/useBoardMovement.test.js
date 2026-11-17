import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import useBoardMovement from './useBoardMovement';
import api from '../services/api';

vi.mock('../services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe('useBoardMovement hook', () => {
    const gameId = 'TEST01';

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('fetches reachable boxes and updates state on successful GET', async () => {
        const mockBoxes = [
            { id: 10, x: 5, y: 5, type: 'PATH', room: null },
            { id: 20, x: 6, y: 5, type: 'DOOR', room: 'KITCHEN' },
        ];
        api.get.mockResolvedValueOnce({ data: mockBoxes });

        const { result } = renderHook(() => useBoardMovement(gameId));

        expect(result.current.reachableBoxes).toEqual([]);
        expect(result.current.isLoadingReachable).toBe(false);

        let returnedBoxes;
        await act(async () => {
            returnedBoxes = await result.current.fetchReachableBoxes(3);
        });

        expect(api.get).toHaveBeenCalledWith('/games/TEST01/board/reachable', {
            params: { diceRoll: 3 },
        });
        expect(result.current.reachableBoxes).toEqual(mockBoxes);
        expect(returnedBoxes).toEqual(mockBoxes);
        expect(result.current.error).toBeNull();
    });

    it('handles error when fetching reachable boxes fails', async () => {
        api.get.mockRejectedValueOnce({
            response: { data: { message: 'Partida no encontrada' } },
        });

        const { result } = renderHook(() => useBoardMovement(gameId));

        await act(async () => {
            await result.current.fetchReachableBoxes(2);
        });

        expect(result.current.reachableBoxes).toEqual([]);
        expect(result.current.error).toBe('Partida no encontrada');
    });

    it('handles box click by sending POST move and triggers onMoveSuccess', async () => {
        const onMoveSuccess = vi.fn();
        const moveResponseData = {
            boxId: 25,
            x: 7,
            y: 5,
            type: 'PATH',
            room: null,
            message: 'Movimiento realizado con éxito',
        };
        api.get.mockResolvedValueOnce({ data: [{ id: 25, x: 7, y: 5 }] });
        api.post.mockResolvedValueOnce({ data: moveResponseData });

        const { result } = renderHook(() => useBoardMovement(gameId, onMoveSuccess));

        // Primer paso: obtener alcanzables para fijar currentDiceRoll
        await act(async () => {
            await result.current.fetchReachableBoxes(4);
        });

        // Segundo paso: hacer clic en una casilla
        let res;
        await act(async () => {
            res = await result.current.handleBoxClick(25);
        });

        expect(api.post).toHaveBeenCalledWith('/games/TEST01/board/move', {
            targetBoxId: 25,
            diceRoll: 4,
        });
        expect(onMoveSuccess).toHaveBeenCalledWith(moveResponseData);
        expect(res).toEqual(moveResponseData);
        expect(result.current.reachableBoxes).toEqual([]); // Se limpian las alcanzables tras mover
        expect(result.current.error).toBeNull();
    });

    it('handles 400 Bad Request on illegal move and stores error message', async () => {
        api.get.mockResolvedValueOnce({ data: [{ id: 10, x: 5, y: 5 }] });
        api.post.mockRejectedValueOnce({
            response: {
                status: 400,
                data: { message: 'Casilla no alcanzable con la tirada actual' },
            },
        });

        const { result } = renderHook(() => useBoardMovement(gameId));

        await act(async () => {
            await result.current.fetchReachableBoxes(1);
        });

        await act(async () => {
            const res = await result.current.handleBoxClick(999);
            expect(res).toBeNull();
        });

        expect(result.current.error).toBe('Casilla no alcanzable con la tirada actual');
    });
});

