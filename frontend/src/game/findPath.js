import { BOARD_WIDTH, BOARD_HEIGHT, UNCLICKABLE_CELL_IDS } from '../board/boardRooms';

const ORTHOGONAL_DELTAS = [
    [0, 1],
    [0, -1],
    [1, 0],
    [-1, 0],
];

/**
 * Finds a tile-by-tile contiguous path between startBox and targetBox.
 * Guarantees every consecutive element in the returned path has Manhattan distance = 1.
 *
 * @param {Object} startBox - The origin box (current player position)
 * @param {Object} targetBox - The destination box
 * @param {Array<Object>} allBoxes - List of all board boxes with x, y, type, room
 * @param {Set<number>} blockedBoxIds - Set of box IDs currently occupied by other players
 * @returns {Array<Object>} Sequence of contiguous boxes from startBox to targetBox
 */
export default function findPath(startBox, targetBox, allBoxes, blockedBoxIds = new Set()) {
    if (!startBox || !targetBox) return [];
    if (startBox.id === targetBox.id || (startBox.x === targetBox.x && startBox.y === targetBox.y)) {
        return [startBox];
    }

    const boxById = new Map();
    const boxByCoord = new Map();
    for (const box of allBoxes) {
        if (box.id != null) boxById.set(box.id, box);
        if (box.x != null && box.y != null) boxByCoord.set(`${box.x},${box.y}`, box);
    }

    const sBox = (startBox.id != null ? boxById.get(startBox.id) : null)
        || (startBox.x != null ? boxByCoord.get(`${startBox.x},${startBox.y}`) : null)
        || startBox;
    const tBox = (targetBox.id != null ? boxById.get(targetBox.id) : null)
        || (targetBox.x != null ? boxByCoord.get(`${targetBox.x},${targetBox.y}`) : null)
        || targetBox;

    if (sBox.x == null || sBox.y == null || tBox.x == null || tBox.y == null) {
        return [sBox, tBox];
    }

    const startKey = `${sBox.x},${sBox.y}`;
    const targetKey = `${tBox.x},${tBox.y}`;
    const startingRoom = sBox.room || null;

    const queue = [{ box: sBox, hasLeftStartingRoom: false }];
    const parentMap = new Map();
    const visited = new Set([startKey]);

    let reachedTargetKey = null;

    while (queue.length > 0) {
        const { box: current, hasLeftStartingRoom } = queue.shift();
        const currentKey = `${current.x},${current.y}`;

        if (currentKey === targetKey) {
            reachedTargetKey = currentKey;
            break;
        }

        for (const [dx, dy] of ORTHOGONAL_DELTAS) {
            const nx = current.x + dx;
            const ny = current.y + dy;
            if (nx < 0 || nx >= BOARD_WIDTH || ny < 0 || ny >= BOARD_HEIGHT) continue;

            const seqId = ny * BOARD_WIDTH + nx + 1;
            if (UNCLICKABLE_CELL_IDS.has(seqId)) continue;

            const nKey = `${nx},${ny}`;
            if (visited.has(nKey)) continue;

            const neighbor = boxByCoord.get(nKey);
            if (!neighbor || neighbor.type === 'OUT_OF_BOUNDS') continue;

            if (blockedBoxIds.has(neighbor.id) && nKey !== targetKey) continue;

            const nextHasLeft = hasLeftStartingRoom || (Boolean(startingRoom) && current.room !== startingRoom);

            let isMoveAllowed = false;

            // 1. Moving within same room
            if (current.room && neighbor.room && current.room === neighbor.room) {
                if (!(nextHasLeft && neighbor.room === startingRoom)) {
                    isMoveAllowed = true;
                }
            }
            // 2. Moving within corridors (hallways/bonus)
            else if (!current.room && !neighbor.room) {
                isMoveAllowed = true;
            }
            // 3. Hallway -> Room: neighbor MUST be DOOR
            else if (!current.room && neighbor.room) {
                if (neighbor.type === 'DOOR' && !(nextHasLeft && neighbor.room === startingRoom)) {
                    isMoveAllowed = true;
                }
            }
            // 4. Room -> Hallway: current MUST be DOOR
            else if (current.room && !neighbor.room) {
                if (current.type === 'DOOR') {
                    isMoveAllowed = true;
                }
            }
            // 5. Room A -> Room B: connecting doors
            else if (current.room && neighbor.room && current.room !== neighbor.room) {
                if (current.type === 'DOOR' && neighbor.type === 'DOOR') {
                    isMoveAllowed = true;
                }
            }

            if (!isMoveAllowed) continue;

            visited.add(nKey);
            parentMap.set(nKey, currentKey);

            if (nKey === targetKey) {
                reachedTargetKey = nKey;
                break;
            }

            queue.push({ box: neighbor, hasLeftStartingRoom: nextHasLeft });
        }

        if (reachedTargetKey) break;
    }

    if (reachedTargetKey) {
        const path = [];
        let currKey = reachedTargetKey;
        while (currKey) {
            const b = boxByCoord.get(currKey);
            if (b) path.unshift(b);
            currKey = parentMap.get(currKey);
        }
        return path;
    }

    // Fallback: Manhattan orthogonal step generator.
    // Guarantees every step is adjacent so the token animation never teleports or breaks.
    const fallbackPath = [sBox];
    let cx = sBox.x;
    let cy = sBox.y;

    while (cx !== tBox.x || cy !== tBox.y) {
        if (cx !== tBox.x) {
            cx += Math.sign(tBox.x - cx);
        } else if (cy !== tBox.y) {
            cy += Math.sign(tBox.y - cy);
        }
        const coordKey = `${cx},${cy}`;
        const b = boxByCoord.get(coordKey) || { id: `synth-${coordKey}`, x: cx, y: cy };
        fallbackPath.push(b);
    }

    return fallbackPath;
}
