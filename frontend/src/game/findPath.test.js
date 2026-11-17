import { describe, expect, it } from 'vitest';
import findPath from './findPath';
import { BOARD_WIDTH, BOARD_HEIGHT, CELL_GROUPS_MAP } from '../board/boardRooms';

const DOORS = [
    { x: 5, y: 14 },
    { x: 14, y: 13 },
    { x: 13, y: 9 }, { x: 16, y: 7 }, { x: 17, y: 7 },
    { x: 14, y: 5 }, { x: 16, y: 6 }, { x: 17, y: 6 },
    { x: 8, y: 2 }, { x: 6, y: 2 },
    { x: 10, y: 3 },
    { x: 5, y: 4 }, { x: 5, y: 2 },
    { x: 5, y: 10 },
    { x: 9, y: 16 }, { x: 10, y: 16 },
    { x: 10, y: 9 }, { x: 10, y: 11 }, { x: 8, y: 10 }, { x: 11, y: 10 },
];

const doorKeySet = new Set(DOORS.map((d) => `${d.x},${d.y}`));

function getRoomAt(x, y) {
    const numId = y * BOARD_WIDTH + x + 1;
    for (const [groupName, definitions] of Object.entries(CELL_GROUPS_MAP)) {
        for (const item of definitions) {
            if (Array.isArray(item)) {
                const [start, end] = item;
                if (numId >= start && numId <= end) return groupName;
            } else if (item === numId) {
                return groupName;
            }
        }
    }
    return null;
}

const mockBoxes = [];
for (let y = 0; y < BOARD_HEIGHT; y++) {
    for (let x = 0; x < BOARD_WIDTH; x++) {
        const id = y * BOARD_WIDTH + x + 1;
        const room = getRoomAt(x, y);
        const isDoor = doorKeySet.has(`${x},${y}`);
        let type = 'PATH';
        if (room && isDoor) type = 'DOOR';
        else if (room) type = 'ROOM';
        mockBoxes.push({ id, x, y, type, room });
    }
}

function verifyContiguous(path) {
    expect(path.length).toBeGreaterThan(0);
    for (let i = 1; i < path.length; i++) {
        const prev = path[i - 1];
        const curr = path[i];
        const dist = Math.abs(curr.x - prev.x) + Math.abs(curr.y - prev.y);
        expect(dist).toBe(1);
    }
}

describe('findPath algorithm', () => {
    it('returns empty array when start or target is missing', () => {
        expect(findPath(null, mockBoxes[0], mockBoxes)).toEqual([]);
        expect(findPath(mockBoxes[0], null, mockBoxes)).toEqual([]);
    });

    it('returns single-item array when start equals target', () => {
        const start = mockBoxes[50];
        const result = findPath(start, start, mockBoxes);
        expect(result).toEqual([start]);
    });

    it('navigates corridors step-by-step', () => {
        const start = mockBoxes.find((b) => b.x === 10 && b.y === 5);
        const target = mockBoxes.find((b) => b.x === 10 && b.y === 8);
        const path = findPath(start, target, mockBoxes);
        verifyContiguous(path);
        expect(path[0].x).toBe(10);
        expect(path[0].y).toBe(5);
        expect(path[path.length - 1].x).toBe(10);
        expect(path[path.length - 1].y).toBe(8);
    });

    it('navigates from inside a room through a door to a corridor cell', () => {
        const patioTile = mockBoxes.find((b) => b.x === 9 && b.y === 17);
        const corridorTile = mockBoxes.find((b) => b.x === 9 && b.y === 14);
        const path = findPath(patioTile, corridorTile, mockBoxes);
        verifyContiguous(path);
        expect(path[0].x).toBe(9);
        expect(path[0].y).toBe(17);
        expect(path.some((step) => step.type === 'DOOR')).toBe(true);
        expect(path[path.length - 1].x).toBe(9);
        expect(path[path.length - 1].y).toBe(14);
    });

    it('navigates from inside a room to its exit door without skipping', () => {
        const patioTile = mockBoxes.find((b) => b.x === 9 && b.y === 17);
        const patioDoor = mockBoxes.find((b) => b.x === 9 && b.y === 16);
        const path = findPath(patioTile, patioDoor, mockBoxes);
        verifyContiguous(path);
        expect(path).toHaveLength(2);
        expect(path[0]).toBe(patioTile);
        expect(path[1]).toBe(patioDoor);
    });

    it('navigates from corridor into a room through the door', () => {
        const corridorTile = mockBoxes.find((b) => b.x === 9 && b.y === 14);
        const patioTile = mockBoxes.find((b) => b.x === 9 && b.y === 17);
        const path = findPath(corridorTile, patioTile, mockBoxes);
        verifyContiguous(path);
        expect(path[0].x).toBe(9);
        expect(path[0].y).toBe(14);
        expect(path[path.length - 1].x).toBe(9);
        expect(path[path.length - 1].y).toBe(17);
        expect(path.some((step) => step.type === 'DOOR')).toBe(true);
    });

    it('uses Manhattan fallback when completely blocked to guarantee contiguous steps', () => {
        const start = mockBoxes.find((b) => b.x === 9 && b.y === 17);
        const target = mockBoxes.find((b) => b.x === 9 && b.y === 14);
        const blockedDoor = mockBoxes.find((b) => b.x === 9 && b.y === 16);
        const blockedDoor2 = mockBoxes.find((b) => b.x === 10 && b.y === 16);

        const path = findPath(start, target, mockBoxes, new Set([blockedDoor.id, blockedDoor2.id]));
        verifyContiguous(path);
        expect(path[0].x).toBe(9);
        expect(path[0].y).toBe(17);
        expect(path[path.length - 1].x).toBe(9);
        expect(path[path.length - 1].y).toBe(14);
    });
});

