import { useState, useCallback } from 'react';
import api from '../services/api';

/**
 * Custom Hook para gestionar el flujo de movimiento en el tablero:
 * 1. Consultar casillas alcanzables según la tirada (GET /games/:gameId/board/reachable?diceRoll=N)
 * 2. Ejecutar movimiento al pulsar una casilla (POST /games/:gameId/board/move)
 * 3. Gestión de estados de carga, casillas alcanzables y errores (ej. HTTP 400 por movimiento ilegal).
 */
export default function useBoardMovement(gameId, onMoveSuccess) {
    const [reachableBoxes, setReachableBoxes] = useState([]);
    const [isLoadingReachable, setIsLoadingReachable] = useState(false);
    const [isMoving, setIsMoving] = useState(false);
    const [error, setError] = useState(null);
    const [currentDiceRoll, setCurrentDiceRoll] = useState(null);

    const clearError = useCallback(() => setError(null), []);
    const clearReachableBoxes = useCallback(() => {
        setReachableBoxes([]);
        setCurrentDiceRoll(null);
    }, []);

    /**
     * Consulta las casillas alcanzables tras una tirada de dados
     * @param {number} diceRoll - Resultado de la tirada de dados
     */
    const fetchReachableBoxes = useCallback(async (diceRoll) => {
        if (!gameId || diceRoll == null || diceRoll <= 0) return [];
        setIsLoadingReachable(true);
        setError(null);
        setCurrentDiceRoll(diceRoll);

        try {
            const response = await api.get(`/games/${encodeURIComponent(gameId)}/board/reachable`, {
                params: { diceRoll },
            });
            const data = response.data || [];
            setReachableBoxes(data);
            return data;
        } catch (err) {
            const errMsg =
                err.response?.data?.message ||
                (typeof err.response?.data === 'string' ? err.response.data : null) ||
                'Error al calcular las casillas alcanzables.';
            setError(errMsg);
            setReachableBoxes([]);
            return [];
        } finally {
            setIsLoadingReachable(false);
        }
    }, [gameId]);

    /**
     * Realiza la petición POST de movimiento cuando el jugador hace clic en una casilla
     * @param {number} targetBoxId - ID de la casilla destino
     * @param {number} [overrideDiceRoll] - Tirada opcional si no está fijada previamente
     */
    const handleBoxClick = useCallback(async (targetBoxId, overrideDiceRoll) => {
        if (!gameId) return null;
        const diceRoll = overrideDiceRoll ?? currentDiceRoll;
        if (diceRoll == null) {
            setError('No hay tirada de dados activa para realizar el movimiento.');
            return null;
        }

        setIsMoving(true);
        setError(null);

        try {
            const response = await api.post(`/games/${encodeURIComponent(gameId)}/board/move`, {
                targetBoxId,
                diceRoll,
            });

            // Limpiar casillas alcanzables tras el movimiento exitoso
            setReachableBoxes([]);
            setCurrentDiceRoll(null);

            if (onMoveSuccess) {
                onMoveSuccess(response.data);
            }
            return response.data;
        } catch (err) {
            // Manejo del error 400 Bad Request u otros errores del servidor
            const errMsg =
                err.response?.data?.message ||
                (typeof err.response?.data === 'string' ? err.response.data : null) ||
                'Movimiento no permitido.';
            setError(errMsg);
            return null;
        } finally {
            setIsMoving(false);
        }
    }, [gameId, currentDiceRoll, onMoveSuccess]);

    return {
        reachableBoxes,
        isLoadingReachable,
        isMoving,
        error,
        fetchReachableBoxes,
        handleBoxClick,
        clearError,
        clearReachableBoxes,
    };
}

