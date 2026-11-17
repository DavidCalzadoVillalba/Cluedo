export const BOARD_WIDTH = 21;
export const BOARD_HEIGHT = 19;

export const CELL_GROUPS_MAP = {
    BEDROOM: [
        [3, 5], [24, 26], [44, 48], [65, 69], [86, 90], [107, 112], [128, 133]
    ],
    BATHROOM: [
        [7, 10], [28, 31], [49, 52]
    ],
    STUDY: [
        [32, 35],
        [53, 56], [74, 77], [95, 98]
    ],
    GAME_ROOM: [
        [150, 153], [170, 174], [191, 195], [212, 216], [233, 237], [255, 258]
    ],
    KITCHEN: [
        [36, 41], [57, 62], [78, 83], [99, 104], [120, 125], [141, 146]
    ],
    GARAGE: [
        [274, 279], [295, 300], [316, 321], [337, 342], [360, 363],
    ],
    DINING_ROOM: [
        [161, 168], [182, 189], [203, 210], [224, 231], [245, 252]
    ],
    LOUNGE: [
        [267, 272], [288, 293], [309, 314], [330, 335], [351, 356], [372, 377]
    ],
    CENTER: [
        [198, 201], [219, 222], [240, 243]
    ],
    PATIO: [
        [322, 323], [328, 329], [343, 350], [364, 371]
    ]
};

export const UNCLICKABLE_CELL_IDS = new Set([
    1, 2, 6,
    11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
    22, 23, 27, 42, 63, 84, 105, 126, 147, 273, 294, 315, 336, 357, 378, 358, 359,
    43, 64, 85, 106, 127, 148, 149, 169, 190, 211, 232, 253, 254,
    115, 136,
    ...Array.from({ length: BOARD_WIDTH }, (_, i) => 379 + i)
]);

export function getGroupNameByCellId(cellId) {
    const numId = Number(cellId);

    for (const [groupName, definitions] of Object.entries(CELL_GROUPS_MAP)) {
        for (const item of definitions) {
            if (Array.isArray(item)) {
                const [start, end] = item;
                if (numId >= start && numId <= end) {
                    return groupName;
                }
            } else {
                if (item === numId) {
                    return groupName;
                }
            }
        }
    }
    return null;
}
