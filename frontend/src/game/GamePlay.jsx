import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import api from '../services/api';
import Board from '../board/Board';
import { getGroupNameByCellId } from '../board/boardRooms';
import InvestigationNotebook from './InvestigationNotebook';
import AccusationPanel from './AccusationPanel';
import AccusationResultModal from './AccusationResultModal';
import BonusCardEventModal from './BonusCardEventModal';
import BonusRevealNotice from './BonusRevealNotice';
import NeutralCardsModal from './NeutralCardsModal';
import findPath from './findPath';
import PlayerHand from './PlayerHand';
import RefutedCardModal from './RefutedCardModal';
import SolutionNotice from './SolutionNotice';
import SuggestionPanel from './SuggestionPanel';
import TurnActions from './TurnActions';
import './gameplay.css';
import './estilos.css';

import {
    CHARACTER_NAMES,
    ROOM_NAMES,
    WEAPON_NAMES,
    cardMatchesSuggestion,
    getCardDisplayName,
    getCardTypeName,
} from './gameConstants';

const getErrorMessage = (error, fallback) =>
    typeof error.response?.data?.message === 'string'
        ? error.response.data.message
        : typeof error.response?.data === 'string' ? error.response.data : fallback;

const REFRESH_INTERVAL = 3500;
const STEP_ANIMATION_MS = 190;
const AUTO_FINISH_SUGGESTION_DELAY_MS = 2500;

export default function GamePlay() {
    const { gameCode } = useParams();
    const navigate = useNavigate();
    const { username } = useAuth();
    const [gameState, setGameState] = useState(null);
    const [error, setError] = useState('');
    const [rolling, setRolling] = useState(false);
    const [finishingTurn, setFinishingTurn] = useState(false);
    const [secondsLeft, setSecondsLeft] = useState(null);
    const [notebookOpen, setNotebookOpen] = useState(false);
    const [solutionNoticeOpen, setSolutionNoticeOpen] = useState(false);
    const [reachableBoxes, setReachableBoxes] = useState([]);
    const [bonusCardEvent, setBonusCardEvent] = useState(null);
    const [usingBonusCardId, setUsingBonusCardId] = useState(null);
    const [selectedTeleportRoom, setSelectedTeleportRoom] = useState('');
    const [selectedExtraCharacter, setSelectedExtraCharacter] = useState('');
    const [selectedExtraWeapon, setSelectedExtraWeapon] = useState('');
    const [selectedRevealTarget, setSelectedRevealTarget] = useState('');
    const [selectedRevealCardId, setSelectedRevealCardId] = useState('');
    const [animatingPlayer, setAnimatingPlayer] = useState(null);
    const [isMoving, setIsMoving] = useState(false);
    const [isUsingPassage, setIsUsingPassage] = useState(false);
    const [suggestSuspect, setSuggestSuspect] = useState('');
    const [suggestWeapon, setSuggestWeapon] = useState('');
    const [accusationCharacter, setAccusationCharacter] = useState('');
    const [accusationWeapon, setAccusationWeapon] = useState('');
    const [accusationRoom, setAccusationRoom] = useState('');
    const [isAccusing, setIsAccusing] = useState(false);
    const [accusationResult, setAccusationResult] = useState(null);
    const [isSuggesting, setIsSuggesting] = useState(false);
    const [selectedRefuteCardId, setSelectedRefuteCardId] = useState('');
    const [isRefuting, setIsRefuting] = useState(false);
    const [lastResolvedSuggestion, setLastResolvedSuggestion] = useState(null);
    const [refutedModalOpen, setRefutedModalOpen] = useState(false);
    const [refutedModalSuggestion, setRefutedModalSuggestion] = useState(null);
    const [neutralCards, setNeutralCards] = useState([]);
    const [neutralModalOpen, setNeutralModalOpen] = useState(false);
    const [neutralCardsLoading, setNeutralCardsLoading] = useState(false);
    const [neutralCardBusy, setNeutralCardBusy] = useState(false);
    const [neutralCardsError, setNeutralCardsError] = useState('');
    const hasShownSolutionNotice = useRef(false);
    const playedImmediateBonusCardIds = useRef(new Set());
    const autoFinishedTurnId = useRef(null);
    const neutralPromptedSuggestionId = useRef(null);
    const refutedPromptedSuggestionId = useRef(null);

    const fetchGameState = useCallback(async () => {
        try {
            const response = await api.get(`/games/${encodeURIComponent(gameCode)}`);
            setGameState(response.data);
            if (response.data.solutionSaved && !hasShownSolutionNotice.current) {
                hasShownSolutionNotice.current = true;
                setSolutionNoticeOpen(true);
            }
            setError('');
            return response.data;
        } catch (fetchError) {
            setError(getErrorMessage(fetchError, 'No se ha podido cargar el estado de la partida.'));
            return null;
        }
    }, [gameCode]);

    const rollInitialDice = async () => {
        try {
            setRolling(true);
            setError('');
            await api.post(`/games/${encodeURIComponent(gameCode)}/turns/initial-roll`);
            await fetchGameState();
        } catch (rollError) {
            setError(getErrorMessage(rollError, 'No se ha podido realizar la tirada inicial.'));
        } finally {
            setRolling(false);
        }
    };

    const rollTurnDice = async () => {
        if (!gameState?.activeTurn?.id) return;
        try {
            setRolling(true);
            setError('');
            await api.post(`/turns/${gameState.activeTurn.id}/roll`);
            await fetchGameState();
        } catch (rollError) {
            setError(getErrorMessage(rollError, 'No se ha podido lanzar los dados.'));
        } finally {
            setRolling(false);
        }
    };

    const finishCurrentTurn = async () => {
        if (!gameState?.activeTurn?.id) return;
        if (autoFinishedTurnId.current === gameState.activeTurn.id) return;
        autoFinishedTurnId.current = gameState.activeTurn.id;
        try {
            setFinishingTurn(true);
            setError('');
            await api.post(`/turns/${gameState.activeTurn.id}/finish`);
            await fetchGameState();
        } catch (finishError) {
            autoFinishedTurnId.current = null;
            setError(getErrorMessage(finishError, 'No se ha podido finalizar el turno.'));
        } finally {
            setFinishingTurn(false);
        }
    };

    useEffect(() => {
        const expiresAt = gameState?.activeTurn?.expiresAt;
        if (!expiresAt || gameState?.activeTurn?.status !== 'ACTIVE') {
            setSecondsLeft(null);
            return;
        }

        const calculateRemaining = () => {
            const expiryMs = new Date(expiresAt).getTime();
            const diff = Math.max(0, Math.ceil((expiryMs - Date.now()) / 1000));
            return diff;
        };

        setSecondsLeft(calculateRemaining());

        const timerId = window.setInterval(() => {
            const remaining = calculateRemaining();
            setSecondsLeft(remaining);
            if (remaining === 0) {
                fetchGameState();
            }
        }, 1000);

        return () => {
            window.clearInterval(timerId);
        };
    }, [gameState?.activeTurn?.id, gameState?.activeTurn?.expiresAt, gameState?.activeTurn?.status, fetchGameState]);

    useEffect(() => {
        fetchGameState();

        const intervalId = window.setInterval(fetchGameState, REFRESH_INTERVAL);
        return () => {
            window.clearInterval(intervalId);
        };
    }, [fetchGameState]);

    useEffect(() => {
        if (bonusCardEvent) return;
        const unplayedImmediateCard = gameState?.myBonusCards?.find(
            (card) => card.immediate && !playedImmediateBonusCardIds.current.has(card.id),
        );
        if (unplayedImmediateCard) setBonusCardEvent(unplayedImmediateCard);
    }, [bonusCardEvent, gameState?.myBonusCards]);

    useEffect(() => {
        const turn = gameState?.activeTurn;
        const isCurrentPlayersTurn = turn?.player?.user?.username === username;
        if (!isCurrentPlayersTurn || turn?.diceResult == null || turn?.hasMoved) {
            setReachableBoxes([]);
            return undefined;
        }

        let cancelled = false;
        api.get(`/turns/${turn.id}/reachable-boxes`)
            .then((response) => {
                if (!cancelled) setReachableBoxes(response.data || []);
            })
            .catch((fetchError) => {
                if (!cancelled) {
                    setError(getErrorMessage(fetchError, 'No se pudieron cargar las casillas alcanzables.'));
                }
            });

        return () => {
            cancelled = true;
        };
    }, [
        gameState?.activeTurn?.id,
        gameState?.activeTurn?.diceResult,
        gameState?.activeTurn?.hasMoved,
        gameState?.activeTurn?.player?.user?.username,
        username,
    ]);

    useEffect(() => {
        const turn = gameState?.activeTurn;
        const suggestion = gameState?.activeSuggestion || turn?.suggestion;
        const myPlayer = gameState?.players?.find((player) => player.user?.username === username);
        const isMyTurn = turn?.player?.user?.username === username;
        const isMySuggestion = Boolean(
            (suggestion?.suggestedByPlayerId && suggestion.suggestedByPlayerId === myPlayer?.id) ||
            (turn?.player?.id && turn.player.id === myPlayer?.id)
        );

        if (!isMyTurn || !isMySuggestion || !suggestion) return;
        const isResolved = suggestion.status === 'REFUTED' || suggestion.status === 'NOT_REFUTED';
        if (!isResolved) return;
        if (gameState?.players?.length === 2 && suggestion.status === 'NOT_REFUTED') return;
        if (autoFinishedTurnId.current === turn?.id) return;

        setLastResolvedSuggestion(suggestion);

        const timer = window.setTimeout(() => {
            finishCurrentTurn();
        }, AUTO_FINISH_SUGGESTION_DELAY_MS);

        return () => window.clearTimeout(timer);
    }, [
        gameState?.activeTurn?.id,
        gameState?.activeTurn?.player?.id,
        gameState?.activeTurn?.player?.user?.username,
        gameState?.activeTurn?.suggestion,
        gameState?.activeSuggestion,
        gameState?.players,
        username,
    ]);

    useEffect(() => {
        const suggestion = gameState?.activeSuggestion || gameState?.activeTurn?.suggestion || lastResolvedSuggestion;
        const myPlayer = gameState?.players?.find((player) => player.user?.username === username);
        const isMySuggestion = Boolean(
            (suggestion?.suggestedByPlayerId && suggestion.suggestedByPlayerId === myPlayer?.id)
            || (gameState?.activeTurn?.player?.id && gameState.activeTurn.player.id === myPlayer?.id)
        );

        if (!isMySuggestion || !suggestion || suggestion.status !== 'REFUTED' || !suggestion.shownCard) return;
        if (refutedPromptedSuggestionId.current === suggestion.id) return;

        refutedPromptedSuggestionId.current = suggestion.id;
        setRefutedModalSuggestion(suggestion);
        setRefutedModalOpen(true);
    }, [
        gameState?.activeSuggestion,
        gameState?.activeTurn?.suggestion,
        gameState?.activeTurn?.player?.id,
        gameState?.players,
        lastResolvedSuggestion,
        username,
    ]);

    useEffect(() => {
        const suggestion = gameState?.activeSuggestion || gameState?.activeTurn?.suggestion;
        const myPlayer = gameState?.players?.find((player) => player.user?.username === username);
        const isMySuggestion = Boolean(
            (suggestion?.suggestedByPlayerId && suggestion.suggestedByPlayerId === myPlayer?.id)
            || gameState?.activeTurn?.player?.id === myPlayer?.id
        );
        if (gameState?.players?.length !== 2 || suggestion?.status !== 'NOT_REFUTED' || !isMySuggestion) return;
        if (neutralPromptedSuggestionId.current === suggestion.id) return;

        let ignore = false;
        neutralPromptedSuggestionId.current = suggestion.id;
        setNeutralModalOpen(true);
        setNeutralCardsLoading(true);
        setNeutralCardsError('');
        api.get(`/games/${encodeURIComponent(gameCode)}/suggestions/neutral-cards`)
            .then((response) => {
                if (!ignore) setNeutralCards(response.data || []);
            })
            .catch((cardsError) => {
                if (!ignore) setNeutralCardsError(getErrorMessage(cardsError, 'No se pudieron cargar las cartas neutrales.'));
            })
            .finally(() => {
                if (!ignore) setNeutralCardsLoading(false);
            });
        return () => { ignore = true; };
    }, [
        gameCode,
        gameState?.players?.length,
        gameState?.activeSuggestion?.id,
        gameState?.activeSuggestion?.status,
        gameState?.activeTurn?.suggestion?.id,
        gameState?.activeTurn?.suggestion?.status,
        gameState?.activeTurn?.player?.id,
        gameState?.players,
        username,
    ]);

    const useSecretPassage = async () => {
        if (!gameState?.activeTurn?.id) return;
        try {
            setIsUsingPassage(true);
            setError('');
            await api.post(`/turns/${gameState.activeTurn.id}/secret-passage`);
            await fetchGameState();
        } catch (passageError) {
            setError(getErrorMessage(passageError, 'No se ha podido usar el pasadizo secreto.'));
        } finally {
            setIsUsingPassage(false);
        }
    };

    const resetBonusCardSelection = () => {
        setSelectedTeleportRoom('');
        setSelectedExtraCharacter('');
        setSelectedExtraWeapon('');
        setSelectedRevealTarget('');
    };

    const useBonusCard = async (card, options = {}) => {
        if (!gameState?.activeTurn?.id) return;
        const room = options.room ?? selectedTeleportRoom;
        if (card.effect === 'TELEPORT_ANY_ROOM' && !room) {
            setError('Elige una habitación para usar esta carta.');
            return;
        }
        if (card.effect === 'REVEAL_CARD' && !options.targetPlayerId) {
            setError('Elige a qué jugador quieres pedir que revele una carta.');
            return;
        }

        try {
            setUsingBonusCardId(card.id);
            setError('');
            const params = {};
            if (card.effect === 'TELEPORT_ANY_ROOM') params.room = room;
            if (card.effect === 'REVEAL_CARD') params.targetPlayerId = options.targetPlayerId;
            await api.post(
                `/turns/${gameState.activeTurn.id}/bonus-cards/${card.id}/use`,
                null,
                Object.keys(params).length ? { params } : undefined,
            );
            if (bonusCardEvent?.id === card.id) {
                playedImmediateBonusCardIds.current.add(card.id);
                setBonusCardEvent(null);
            }
            resetBonusCardSelection();
            if (card.effect === 'EXTRA_TURN') {
                await api.post(`/turns/${gameState.activeTurn.id}/finish`);
            }
            await fetchGameState();
        } catch (useError) {
            setError(getErrorMessage(useError, 'No se ha podido usar la carta bonus.'));
        } finally {
            setUsingBonusCardId(null);
        }
    };

    const handleDismissBonusModal = async () => {
        setBonusCardEvent(null);
        await finishCurrentTurn();
    };

    const playDrawnBonusCard = async () => {
        if (!bonusCardEvent?.id || !gameState?.activeTurn?.id) return;
        if (bonusCardEvent.effect === 'EXTRA_QUESTION') {
            if (!selectedExtraCharacter || !selectedExtraWeapon || !selectedTeleportRoom) {
                setError('Selecciona un sospechoso, un arma y una habitación para formular la pregunta extra.');
                return;
            }
            try {
                setUsingBonusCardId(bonusCardEvent.id);
                setError('');
                await api.post(`/games/${encodeURIComponent(gameCode)}/suggestions/bonus-question`, {
                    bonusCardId: bonusCardEvent.id,
                    character: selectedExtraCharacter,
                    weapon: selectedExtraWeapon,
                    room: selectedTeleportRoom,
                });
                playedImmediateBonusCardIds.current.add(bonusCardEvent.id);
                setBonusCardEvent(null);
                resetBonusCardSelection();
                await fetchGameState();
            } catch (useError) {
                setError(getErrorMessage(useError, 'No se ha podido formular la pregunta extra.'));
            } finally {
                setUsingBonusCardId(null);
            }
            return;
        }
        if (bonusCardEvent.effect === 'REVEAL_CARD') {
            await useBonusCard(bonusCardEvent, { targetPlayerId: Number(selectedRevealTarget) });
            await finishCurrentTurn();
            return;
        }
        if (bonusCardEvent.effect === 'TELEPORT_ANY_ROOM') {
            await useBonusCard(bonusCardEvent, { room: selectedTeleportRoom });
            return;
        }
        if (bonusCardEvent.effect === 'EXTRA_DICE_6') {
            await useBonusCard(bonusCardEvent);
            return;
        }
        if (bonusCardEvent.effect !== 'EXTRA_TURN') return;
        await useBonusCard(bonusCardEvent);
    };

    const handlePeekCard = async (card) => {
        if (!card?.id) return;
        try {
            setUsingBonusCardId(card.id);
            setError('');
            await api.post(`/games/${encodeURIComponent(gameCode)}/suggestions/peek`, null, {
                params: { bonusCardId: card.id },
            });
            await fetchGameState();
        } catch (useError) {
            setError(getErrorMessage(useError, 'No se ha podido usar Echa un vistazo.'));
        } finally {
            setUsingBonusCardId(null);
        }
    };

    const handleNeutralCardSelect = async (cardId) => {
        try {
            setNeutralCardBusy(true);
            setNeutralCardsError('');
            await api.post(`/games/${encodeURIComponent(gameCode)}/suggestions/neutral-cards/${cardId}/consult`);
            const response = await api.get(`/games/${encodeURIComponent(gameCode)}/suggestions/neutral-cards`);
            setNeutralCards(response.data || []);
        } catch (consultError) {
            setNeutralCardsError(getErrorMessage(consultError, 'No se pudo consultar la carta neutral.'));
        } finally {
            setNeutralCardBusy(false);
        }
    };

    const handleCloseNeutralCards = async () => {
        setNeutralModalOpen(false);
        const suggestion = gameState?.activeSuggestion || gameState?.activeTurn?.suggestion;
        neutralPromptedSuggestionId.current = suggestion?.id ?? neutralPromptedSuggestionId.current;
        await finishCurrentTurn();
    };

    const handleRevealSelectedCard = async () => {
        const pendingReveal = gameState?.pendingBonusReveals?.[0];
        if (!pendingReveal || !selectedRevealCardId) return;
        try {
            setUsingBonusCardId(`reveal-${pendingReveal.id}`);
            setError('');
            await api.post(
                `/games/${encodeURIComponent(gameCode)}/bonus-card-reveals/${pendingReveal.id}/reveal`,
                { cardId: Number(selectedRevealCardId) },
            );
            setSelectedRevealCardId('');
            await fetchGameState();
        } catch (revealError) {
            setError(getErrorMessage(revealError, 'No se ha podido revelar la carta.'));
        } finally {
            setUsingBonusCardId(null);
        }
    };

    if (error && !gameState) {
        return (
            <main className="gameplay-page gameplay-error-container">
                <div className="gameplay-error-card" role="alert">
                    <h1>Error de conexión</h1>
                    <p>{error}</p>
                    <button className="btn-cancel" onClick={() => navigate('/')}>
                        Volver al inicio
                    </button>
                </div>
            </main>
        );
    }

    if (!gameState) {
        return (
            <main className="gameplay-page gameplay-loading-container">
                <div className="gameplay-loading-spinner" aria-hidden="true" />
                <p>Cargando partida...</p>
            </main>
        );
    }

    const players = gameState.players || [];
    const activePlayer = gameState.activeTurn?.player;
    const playersWaitingForInitialRoll = players.filter((player) => player.initialDiceResult == null);
    const isInitialRoll = playersWaitingForInitialRoll.length > 0;
    const myPlayer = players.find((player) => player.user?.username === username);
    const canRollInitial = isInitialRoll && myPlayer && myPlayer.initialDiceResult == null;
    const isMyTurn = activePlayer?.user?.username === username;
    const myHand = gameState.myHand || [];
    const myBonusCards = gameState.myBonusCards || [];
    const faceUpCards = gameState.faceUpCards || [];
    const activeBox = activePlayer?.currentBox;
    const currentRoom = activeBox?.room || (activeBox?.x != null && activeBox?.y != null ? getGroupNameByCellId(activeBox.y * 21 + activeBox.x + 1) : null);
    const isInCardRoom = currentRoom && currentRoom !== 'CENTER';
    const SECRET_PASSAGE_DESTINATIONS = {
        KITCHEN: 'GARAGE',
        GARAGE: 'KITCHEN',
        BEDROOM: 'LOUNGE',
        LOUNGE: 'BEDROOM',
    };
    const passageDestinationRoom = currentRoom ? SECRET_PASSAGE_DESTINATIONS[currentRoom] : null;
    const activeSuggestion = gameState.activeSuggestion || gameState.activeTurn?.suggestion;
    const hasSuggested = Boolean(activeSuggestion);
    const isHypothesisPending = activeSuggestion?.status === 'PENDING';
    const isMyTurnToRefute = isHypothesisPending && (
        activeSuggestion?.currentResponderId === myPlayer?.id ||
        activeSuggestion?.currentResponder?.id === myPlayer?.id
    );
    const isMySuggestion = Boolean(
        (activeSuggestion?.suggestedByPlayerId && activeSuggestion.suggestedByPlayerId === myPlayer?.id) ||
        (activePlayer?.id && activePlayer.id === myPlayer?.id)
    );
    const skipRefutationBonusCard = myBonusCards.find((card) => card.effect === 'SKIP_REFUTATION');
    const pendingBonusReveals = gameState.pendingBonusReveals || [];
    const pendingBonusReveal = pendingBonusReveals[0];
    const publicBonusReveals = gameState.publicBonusReveals || [];
    const selectableRevealPlayers = players.filter((player) =>
        player.id !== myPlayer?.id && !player.eliminated
    );
    const bonusRoomOptions = Object.entries(ROOM_NAMES).filter(([room]) => room !== 'CENTER');
    const bonusCharacterOptions = Object.entries(CHARACTER_NAMES);
    const bonusWeaponOptions = Object.entries(WEAPON_NAMES);
    const matchingCards = (isMyTurnToRefute && activeSuggestion)
        ? myHand.filter((card) => cardMatchesSuggestion(card, activeSuggestion))
        : [];
    const hasRolledTurn = Boolean(gameState?.activeTurn?.diceResult != null || gameState?.activeTurn?.hasMoved);
    const canSuggest = isMyTurn && isInCardRoom && !hasSuggested && !isMoving && !rolling && !myPlayer?.eliminated && hasRolledTurn;
    const canUsePassage = Boolean(
        isMyTurn &&
        gameState?.activeTurn?.status === 'ACTIVE' &&
        !gameState?.activeTurn?.hasMoved &&
        gameState?.activeTurn?.diceResult == null &&
        !isMoving &&
        !rolling &&
        Boolean(passageDestinationRoom)
    );
    const canAccuse = isMyTurn
        && currentRoom === 'CENTER'
        && !myPlayer?.eliminated
        && !hasSuggested
        && !isMoving
        && !rolling
        && !isInitialRoll
        && gameState.status === 'STARTED';
    const finalWinner = gameState.winner || accusationResult?.winner;
    const finalSolution = gameState.solution || accusationResult?.solution;
    const gameEnded = gameState.status === 'ENDED' || accusationResult?.gameStatus === 'ENDED';
    const resultMode = gameEnded
        ? finalWinner
            ? finalWinner.user?.username === username ? 'victory' : 'spectator-winner'
            : 'no-winner'
        : accusationResult?.correct === false ? 'defeat' : null;
    const resultPlayer = resultMode === 'victory' || resultMode === 'spectator-winner' ? finalWinner : myPlayer;
    const resultPlayerName = resultPlayer?.user?.username;
    const resultPlayerCharacter = CHARACTER_NAMES[resultPlayer?.character] || resultPlayer?.character;
    const canPlayDrawnBonusCard = Boolean(
        bonusCardEvent?.id
        && isMyTurn
        && (
            (bonusCardEvent.effect === 'EXTRA_TURN' && !isHypothesisPending)
            || bonusCardEvent.effect === 'EXTRA_DICE_6'
            || (bonusCardEvent.effect === 'TELEPORT_ANY_ROOM' && selectedTeleportRoom)
            || (bonusCardEvent.effect === 'EXTRA_QUESTION'
                && selectedExtraCharacter && selectedExtraWeapon && selectedTeleportRoom && !hasSuggested)
            || (bonusCardEvent.effect === 'REVEAL_CARD' && selectedRevealTarget)
        )
    );
    const peekCard = myBonusCards.find((card) => card.effect === 'PEEK_CARD');

    const moveToCell = async (cell) => {
        if (isMoving) return;
        if (!gameState?.activeTurn?.id) return;

        const turnPlayer = gameState.activeTurn.player;
        const allBoxes = gameState.board?.boxes || [];
        const boxById = new Map(allBoxes.map((box) => [box.id, box]));

        const rawStartBox = turnPlayer?.currentBox;
        const startBox = (rawStartBox?.id != null ? boxById.get(rawStartBox.id) : null) || rawStartBox;

        let targetBox = reachableBoxes.find((box) => box.id === cell.id);
        if (!targetBox && (cell.room || cell.group)) {
            const roomName = cell.room || cell.group;
            const roomMatches = reachableBoxes.filter((box) => box.room === roomName);
            if (roomMatches.length > 0 && startBox?.x != null && startBox?.y != null) {
                targetBox = roomMatches.reduce((closest, curr) => {
                    const distCurr = Math.abs(curr.x - startBox.x) + Math.abs(curr.y - startBox.y);
                    const distClosest = Math.abs(closest.x - startBox.x) + Math.abs(closest.y - startBox.y);
                    return distCurr < distClosest ? curr : closest;
                }, roomMatches[0]);
            } else {
                targetBox = roomMatches[0];
            }
        }
        if (!targetBox) return;

        if (targetBox.id != null && boxById.has(targetBox.id)) {
            targetBox = { ...boxById.get(targetBox.id), ...targetBox };
        }

        const blockedIds = new Set(
            (gameState.players || [])
                .filter((player) => player.id !== turnPlayer?.id && player.currentBox?.room == null && player.currentBox?.id != null)
                .map((player) => player.currentBox.id)
        );

        const path = findPath(startBox, targetBox, allBoxes, blockedIds);

        let shouldAutoFinish = false;
        try {
            setIsMoving(true);
            setError('');

            if (path.length > 1) {
                for (let i = 1; i < path.length; i++) {
                    const step = path[i];
                    const prevStep = path[i - 1];
                    setAnimatingPlayer({
                        id: turnPlayer.id,
                        x: step.x,
                        y: step.y,
                        boxId: step.id,
                        fromX: prevStep.x,
                        fromY: prevStep.y,
                        stepIndex: i,
                        totalSteps: path.length - 1,
                    });
                    await new Promise((resolve) => setTimeout(resolve, STEP_ANIMATION_MS));
                }
            }

            const previousBonusCardIds = new Set(myBonusCards.map((card) => card.id));
            const response = await api.post(`/turns/${gameState.activeTurn.id}/move`, null, {
                params: { boxId: targetBox.id },
            });
            setReachableBoxes([]);
            const updatedGameState = await fetchGameState();
            const landedOnBonus = typeof response.data?.landedOnBonus === 'boolean'
                ? response.data.landedOnBonus
                : targetBox.type === 'BONUS';
            if (landedOnBonus) {
                const newlyAddedCard = updatedGameState?.myBonusCards?.find(
                    (card) => !previousBonusCardIds.has(card.id),
                );
                setBonusCardEvent(
                    response.data?.drawnBonusCard
                    || newlyAddedCard
                    || { unavailable: true },
                );
            } else if (!targetBox.room) {
                shouldAutoFinish = true;
            }
        } catch (moveError) {
            setError(getErrorMessage(moveError, 'No se ha podido confirmar el movimiento.'));
        } finally {
            setAnimatingPlayer(null);
            setIsMoving(false);
        }

        if (shouldAutoFinish) {
            await finishCurrentTurn();
        }
    };

    const handleAccuse = async () => {
        if (!accusationCharacter || !accusationWeapon || !accusationRoom) return;
        try {
            setIsAccusing(true);
            setError('');
            const response = await api.post(`/games/${encodeURIComponent(gameCode)}/accusations`, {
                character: accusationCharacter,
                weapon: accusationWeapon,
                room: accusationRoom,
            });
            setAccusationResult(response.data);
            setAccusationCharacter('');
            setAccusationWeapon('');
            setAccusationRoom('');
            await fetchGameState();
        } catch (accusationError) {
            setError(getErrorMessage(accusationError, 'No se ha podido formular la acusación.'));
        } finally {
            setIsAccusing(false);
        }
    };

    const handleSuggest = async () => {
        if (!suggestSuspect || !suggestWeapon || !currentRoom) return;
        try {
            setIsSuggesting(true);
            setError('');
            setLastResolvedSuggestion(null);
            setRefutedModalSuggestion(null);
            setRefutedModalOpen(false);
            refutedPromptedSuggestionId.current = null;
            await api.post(`/games/${encodeURIComponent(gameCode)}/suggestions`, {
                character: suggestSuspect,
                weapon: suggestWeapon,
                room: currentRoom,
            });
            setSuggestSuspect('');
            setSuggestWeapon('');
            await fetchGameState();
        } catch (suggestError) {
            setError(getErrorMessage(suggestError, 'No se ha podido formular la hipótesis.'));
        } finally {
            setIsSuggesting(false);
        }
    };

    const handleRefute = async () => {
        const cardId = selectedRefuteCardId || (matchingCards.length === 1 ? matchingCards[0].id : null);
        if (!cardId) return;
        try {
            setIsRefuting(true);
            setError('');
            await api.post(`/games/${encodeURIComponent(gameCode)}/suggestions/refute`, {
                cardId: Number(cardId),
            });
            setSelectedRefuteCardId('');
            await fetchGameState();
        } catch (refuteError) {
            setError(getErrorMessage(refuteError, 'No se ha podido refutar la hipótesis.'));
        } finally {
            setIsRefuting(false);
        }
    };

    const handlePass = async (useSkipBonus = false) => {
        try {
            setIsRefuting(true);
            setError('');
            const params = useSkipBonus && skipRefutationBonusCard ? { bonusCardId: skipRefutationBonusCard.id } : undefined;
            await api.post(`/games/${encodeURIComponent(gameCode)}/suggestions/pass`, null, { params });
            setSelectedRefuteCardId('');
            await fetchGameState();
        } catch (passError) {
            setError(getErrorMessage(passError, 'No se ha podido pasar la refutación.'));
        } finally {
            setIsRefuting(false);
        }
    };

    return (
        <div className="gameplay-page">
            <BonusCardEventModal
                card={bonusCardEvent}
                onPlay={playDrawnBonusCard}
                onWait={handleDismissBonusModal}
                isPlaying={usingBonusCardId === bonusCardEvent?.id}
                canPlay={canPlayDrawnBonusCard}
                rooms={bonusRoomOptions}
                characters={bonusCharacterOptions}
                weapons={bonusWeaponOptions}
                players={selectableRevealPlayers}
                selectedRoom={selectedTeleportRoom}
                onRoomChange={setSelectedTeleportRoom}
                selectedCharacter={selectedExtraCharacter}
                onCharacterChange={setSelectedExtraCharacter}
                selectedWeapon={selectedExtraWeapon}
                onWeaponChange={setSelectedExtraWeapon}
                selectedTarget={selectedRevealTarget}
                onTargetChange={setSelectedRevealTarget}
            />
            <BonusRevealNotice
                bonusCardEvent={bonusCardEvent}
                pendingBonusReveal={pendingBonusReveal}
                publicBonusReveals={publicBonusReveals}
                myHand={myHand}
                selectedRevealCardId={selectedRevealCardId}
                usingBonusCardId={usingBonusCardId}
                onRevealCardChange={setSelectedRevealCardId}
                onReveal={handleRevealSelectedCard}
            />
            <SolutionNotice
                isOpen={solutionNoticeOpen && gameState.solutionSaved}
                onClose={() => setSolutionNoticeOpen(false)}
            />
            <AccusationResultModal
                mode={resultMode}
                playerName={resultPlayerName}
                playerCharacter={resultPlayerCharacter}
                solution={finalSolution}
                onClose={() => setAccusationResult(null)}
                onReturnToMenu={() => navigate('/')}
            />
            <RefutedCardModal
                isOpen={refutedModalOpen}
                onClose={() => setRefutedModalOpen(false)}
                refutedSuggestion={refutedModalSuggestion}
                onOpenNotebook={() => setNotebookOpen(true)}
            />
            <InvestigationNotebook
                gameCode={gameCode}
                myHand={myHand}
                isOpen={notebookOpen}
                onClose={() => setNotebookOpen(false)}
            />

            <main className="gameplay-main">
                <section className="gameplay-board-section" aria-label="Tablero de juego">
                    <Board
                        width="min(760px, calc((100dvh - clamp(110px, 15vh, 160px)) * 21 / 19), calc(100vw - clamp(260px, 24vw, 380px)))"
                        boxes={gameState.board?.boxes || []}
                        players={gameState.players || []}
                        weapons={gameState.weaponTokens || []}
                        characterTokens={gameState.characterTokens || []}
                        activeTurn={gameState.activeTurn}
                        cols={21}
                        rows={19}
                        currentUsername={username}
                        onCellClick={moveToCell}
                        reachableBoxIds={reachableBoxes.map((box) => box.id)}
                        onNotebookOpen={() => setNotebookOpen(true)}
                        onInitialRoll={rollInitialDice}
                        isRollingInitial={rolling}
                        onRollTurn={rollTurnDice}
                        isRollingTurn={rolling}
                        isMoving={isMoving}
                        secondsLeft={secondsLeft}
                        onFinishTurn={finishCurrentTurn}
                        isFinishingTurn={finishingTurn}
                        isHypothesisPending={isHypothesisPending}
                        canUsePassage={canUsePassage}
                        onSecretPassage={useSecretPassage}
                        isUsingPassage={isUsingPassage}
                        currentRoom={currentRoom}
                        passageDestinationRoom={passageDestinationRoom}
                        animatingPlayer={animatingPlayer}
                        myHand={myHand}
                        myBonusCards={myBonusCards}
                        onExitMenu={() => navigate('/')}
                        gameCode={gameCode}
                    />
                </section>

                <TurnActions
                    isInitialRoll={isInitialRoll}
                    canRollInitial={canRollInitial}
                    myInitialDiceResult={myPlayer?.initialDiceResult}
                    isMyTurn={isMyTurn}
                    activePlayerName={activePlayer?.user?.username}
                    currentRoom={currentRoom}
                    activeTurn={gameState.activeTurn}
                    rolling={rolling}
                    isMoving={isMoving}
                    isUsingPassage={isUsingPassage}
                    finishingTurn={finishingTurn}
                    isHypothesisPending={isHypothesisPending}
                    reachableBoxCount={reachableBoxes.length}
                    secondsLeft={secondsLeft}
                    error={error}
                    onRollInitial={rollInitialDice}
                    onRollTurn={rollTurnDice}
                    onSecretPassage={useSecretPassage}
                    onFinishTurn={finishCurrentTurn}
                >
                    <AccusationPanel
                        canAccuse={canAccuse}
                        character={accusationCharacter}
                        weapon={accusationWeapon}
                        room={accusationRoom}
                        isAccusing={isAccusing}
                        onCharacterChange={setAccusationCharacter}
                        onWeaponChange={setAccusationWeapon}
                        onRoomChange={setAccusationRoom}
                        onAccuse={handleAccuse}
                    />
                    <SuggestionPanel
                        canSuggest={canSuggest}
                        currentRoom={currentRoom}
                        suggestSuspect={suggestSuspect}
                        suggestWeapon={suggestWeapon}
                        isSuggesting={isSuggesting}
                        onSuspectChange={setSuggestSuspect}
                        onWeaponChange={setSuggestWeapon}
                        onSuggest={handleSuggest}
                        hasSuggested={hasSuggested}
                        activeSuggestion={activeSuggestion}
                        isHypothesisPending={isHypothesisPending}
                        isMyTurnToRefute={isMyTurnToRefute}
                        isMySuggestion={isMySuggestion}
                        matchingCards={matchingCards}
                        selectedRefuteCardId={selectedRefuteCardId}
                        isRefuting={isRefuting}
                        onRefuteCardChange={setSelectedRefuteCardId}
                        onRefute={handleRefute}
                        skipRefutationBonusCard={skipRefutationBonusCard}
                        onPass={handlePass}
                        myPlayerId={myPlayer?.id}
                        activePlayerName={activePlayer?.user?.username}
                        usingBonusCardId={usingBonusCardId}
                        hasPeekCard={Boolean(peekCard)}
                        onPeek={() => handlePeekCard(peekCard)}
                        myHand={myHand}
                        players={players}
                        hasMoved={Boolean(gameState?.activeTurn?.hasMoved)}
                        diceResult={gameState?.activeTurn?.diceResult}
                        onOpenRefutedCardModal={() => setRefutedModalOpen(true)}
                    />
                    {lastResolvedSuggestion && !activeSuggestion && (
                        <div className="gameplay-suggestion-summary gameplay-resolved-banner" role="status">
                            {lastResolvedSuggestion.status === 'REFUTED' ? (
                                lastResolvedSuggestion.shownCard ? (
                                    <span>
                                        Tu hipótesis fue refutada por <strong>{lastResolvedSuggestion.refutedByPlayerName || 'un jugador'}</strong>.{' '}
                                        <button
                                            type="button"
                                            className="gameplay-refuted-reopen-btn"
                                            onClick={() => setRefutedModalOpen(true)}
                                        >
                                            👁️ Ver carta mostrada
                                        </button>
                                    </span>
                                ) : (
                                    <span>
                                        <strong>Tu hipótesis ha sido refutada.</strong>
                                    </span>
                                )
                            ) : (
                                <span>
                                    <strong>Nadie ha podido refutar tu hipótesis.</strong>
                                </span>
                            )}
                            <button
                                type="button"
                                className="gameplay-dismiss-btn"
                                onClick={() => setLastResolvedSuggestion(null)}
                                title="Cerrar aviso de hipótesis"
                            >
                                ✕ Cerrar
                            </button>
                        </div>
                    )}
                </TurnActions>

                {neutralModalOpen && (
                    <NeutralCardsModal
                        cards={neutralCards}
                        loading={neutralCardsLoading}
                        error={neutralCardsError}
                        busy={neutralCardBusy}
                        onSelect={handleNeutralCardSelect}
                        onClose={handleCloseNeutralCards}
                    />
                )}

                <div className="gameplay-offscreen-panel">
                    <PlayerHand
                        myHand={myHand}
                        myBonusCards={myBonusCards}
                        faceUpCards={faceUpCards}
                        isMyTurn={isMyTurn}
                        activeTurn={gameState.activeTurn}
                        activeSuggestion={activeSuggestion}
                        myPlayerId={myPlayer?.id}
                        selectedTeleportRoom={selectedTeleportRoom}
                        onTeleportRoomChange={setSelectedTeleportRoom}
                        usingBonusCardId={usingBonusCardId}
                        onUseBonusCard={useBonusCard}
                        onPeek={handlePeekCard}
                    />
                </div>
            </main>
        </div>
    );
}
