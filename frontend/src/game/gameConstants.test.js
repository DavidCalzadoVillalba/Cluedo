import { describe, expect, test } from 'vitest';
import {
    CHARACTER_IMAGES,
    getCardDisplayName,
    getCardImage,
    getBonusCardInfo,
    ROOM_IMAGES,
    WEAPON_IMAGES,
    WEAPON_TOKENS_3D,
    CHARACTER_SELECT_CARDS,
} from './gameConstants';

describe('getCardImage', () => {
    test('provides artwork mappings for all 21 character, weapon, and room cards', () => {
        expect(Object.keys(CHARACTER_IMAGES)).toHaveLength(6);
        expect(Object.keys(WEAPON_IMAGES)).toHaveLength(6);
        expect(Object.keys(ROOM_IMAGES)).toHaveLength(9);
    });

    test('returns the illustration for each supported card type', () => {
        expect(getCardImage({ character: 'PRADO' })).toBe('/images/personajes/Prado.png');
        expect(getCardImage({ weapon: 'LLAVE_INGLESA' })).toBe('/images/armas/Herramienta.png');
        expect(getCardImage({ room: 'STUDY' })).toBe('/images/habitaciones/estudio.png');
        expect(getCardDisplayName({ weapon: 'LLAVE_INGLESA' })).toBe('Herramienta');
    });

    test('handles lowercase card values and unknown cards', () => {
        expect(getCardImage({ character: 'amapola' })).toBe('/images/personajes/Amapola.png');
        expect(getCardImage({ cardType: 'BONUS' })).toBeUndefined();
        expect(getCardImage(null)).toBeUndefined();
    });

    test('describes stored bonus cards for the player hand', () => {
        expect(getBonusCardInfo({ effect: 'EXTRA_DICE_6' })).toMatchObject({
            name: 'Suma 6',
            image: '/images/cartasbonus/suma6.png',
        });
        expect(getBonusCardInfo({ effect: 'SKIP_REFUTATION' }).description).toContain('refutación');
    });

    test('defines 3D miniature tokens for all 6 weapons', () => {
        expect(Object.keys(WEAPON_TOKENS_3D)).toHaveLength(6);
        expect(WEAPON_TOKENS_3D.PISTOLA).toBe('/images/armas/pistola-3d.png');
        expect(WEAPON_TOKENS_3D.CANDELABRO).toBe('/images/armas/candelabro-3d.png');
        expect(WEAPON_TOKENS_3D.CUCHILLO).toBe('/images/armas/cuchillo-3d.png');
        expect(WEAPON_TOKENS_3D.TUBERIA_PLOMO).toBe('/images/armas/tuberia-plomo-3d.png');
        expect(WEAPON_TOKENS_3D.CUERDA).toBe('/images/armas/cuerda-3d.png');
        expect(WEAPON_TOKENS_3D.LLAVE_INGLESA).toBe('/images/armas/herramienta-3d.png');
    });

    test('defines character select portrait cards for all 6 characters', () => {
        expect(Object.keys(CHARACTER_SELECT_CARDS)).toHaveLength(6);
        expect(CHARACTER_SELECT_CARDS.AMAPOLA).toBe('/images/personajes/amapola-card.png');
        expect(CHARACTER_SELECT_CARDS.RUBIO).toBe('/images/personajes/rubio-card.png');
        expect(CHARACTER_SELECT_CARDS.BLANCO).toBe('/images/personajes/blanco-card.png');
        expect(CHARACTER_SELECT_CARDS.PRADO).toBe('/images/personajes/prado-card.png');
        expect(CHARACTER_SELECT_CARDS.CELESTE).toBe('/images/personajes/celeste-card.png');
        expect(CHARACTER_SELECT_CARDS.MORA).toBe('/images/personajes/mora-card.png');
    });
});
