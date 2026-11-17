export const CHARACTERS = [
    { id: 'amapola', name: 'Amapola', apiValue: 'AMAPOLA', colorClass: 'color-amapola' },
    { id: 'rubio', name: 'Rubio', apiValue: 'RUBIO', colorClass: 'color-rubio' },
    { id: 'blanco', name: 'Blanco', apiValue: 'BLANCO', colorClass: 'color-blanco' },
    { id: 'prado', name: 'Prado', apiValue: 'PRADO', colorClass: 'color-prado' },
    { id: 'celeste', name: 'Celeste', apiValue: 'CELESTE', colorClass: 'color-celeste' },
    { id: 'mora', name: 'Mora', apiValue: 'MORA', colorClass: 'color-mora' },
];

export const CHARACTER_NAMES = {
    AMAPOLA: 'Amapola',
    RUBIO: 'Rubio',
    BLANCO: 'Blanco',
    PRADO: 'Prado',
    CELESTE: 'Celeste',
    MORA: 'Mora',
};

export const CHARACTER_CLASSES = {
    AMAPOLA: 'color-amapola',
    RUBIO: 'color-rubio',
    BLANCO: 'color-blanco',
    PRADO: 'color-prado',
    CELESTE: 'color-celeste',
    MORA: 'color-mora',
};

export const CHARACTER_IMAGES = {
    AMAPOLA: '/images/personajes/Amapola.png',
    RUBIO: '/images/personajes/Rubio.png',
    BLANCO: '/images/personajes/Blanco.png',
    PRADO: '/images/personajes/Prado.png',
    CELESTE: '/images/personajes/Celeste.png',
    MORA: '/images/personajes/Mora.png',
};

export const CHARACTER_TOKENS_3D = {
    AMAPOLA: '/images/fichas/amapola-3d.png',
    RUBIO: '/images/fichas/rubio-3d.png',
    BLANCO: '/images/fichas/blanco-3d.png',
    PRADO: '/images/fichas/prado-3d.png',
    CELESTE: '/images/fichas/celeste-3d.png',
    MORA: '/images/fichas/mora-3d.png',
};

export const CHARACTER_SELECT_CARDS = {
    AMAPOLA: '/images/personajes/amapola-card.png',
    RUBIO: '/images/personajes/rubio-card.png',
    BLANCO: '/images/personajes/blanco-card.png',
    PRADO: '/images/personajes/prado-card.png',
    CELESTE: '/images/personajes/celeste-card.png',
    MORA: '/images/personajes/mora-card.png',
};

export const WEAPON_NAMES = {
    CANDELABRO: 'Candelabro',
    CUCHILLO: 'Cuchillo',
    TUBERIA_PLOMO: 'Tubería de plomo',
    PISTOLA: 'Pistola',
    CUERDA: 'Cuerda',
    LLAVE_INGLESA: 'Herramienta',
};

export const WEAPON_IMAGES = {
    CANDELABRO: '/images/armas/Candelabro.png',
    CUCHILLO: '/images/armas/Puñal.png',
    TUBERIA_PLOMO: '/images/armas/Tuberia de plomo.png',
    PISTOLA: '/images/armas/Pistola.png',
    CUERDA: '/images/armas/Cuerda.png',
    LLAVE_INGLESA: '/images/armas/Herramienta.png',
};

export const WEAPON_TOKENS_3D = {
    PISTOLA: '/images/armas/pistola-3d.png',
    CANDELABRO: '/images/armas/candelabro-3d.png',
    CUCHILLO: '/images/armas/cuchillo-3d.png',
    TUBERIA_PLOMO: '/images/armas/tuberia-plomo-3d.png',
    CUERDA: '/images/armas/cuerda-3d.png',
    LLAVE_INGLESA: '/images/armas/herramienta-3d.png',
};

export const ROOM_NAMES = {
    KITCHEN: 'Cocina',
    DINING_ROOM: 'Comedor',
    LOUNGE: 'Salón',
    PATIO: 'Patio',
    GAME_ROOM: 'Sala de juegos',
    STUDY: 'Estudio',
    BEDROOM: 'Dormitorio',
    BATHROOM: 'Baño',
    GARAGE: 'Garaje',
    CENTER: 'Centro',
};

export const ROOM_IMAGES = {
    KITCHEN: '/images/habitaciones/cocina.png',
    DINING_ROOM: '/images/habitaciones/comedor.png',
    LOUNGE: '/images/habitaciones/salon.png',
    PATIO: '/images/habitaciones/patio.png',
    GAME_ROOM: '/images/habitaciones/Saladejuegos.png',
    STUDY: '/images/habitaciones/estudio.png',
    BEDROOM: '/images/habitaciones/dormitorio.png',
    BATHROOM: '/images/habitaciones/baño.png',
    GARAGE: '/images/habitaciones/garaje.png',
};

export const getCardImage = (card) => {
    if (!card) return undefined;
    const char = card.character || ((card.type === 'CHARACTER' || card.cardType === 'CHARACTER') ? card.name : null);
    const weap = card.weapon || ((card.type === 'WEAPON' || card.cardType === 'WEAPON') ? card.name : null);
    const rm = card.room || ((card.type === 'ROOM' || card.cardType === 'ROOM') ? card.name : null);
    if (char) return CHARACTER_IMAGES[String(char).toUpperCase()];
    if (weap) return WEAPON_IMAGES[String(weap).toUpperCase()];
    if (rm) return ROOM_IMAGES[String(rm).toUpperCase()];
    return undefined;
};

export const getCardDisplayName = (card) => {
    if (!card) return 'Carta desconocida';
    if (card.character) return CHARACTER_NAMES[card.character] || card.character;
    if (card.weapon) return WEAPON_NAMES[card.weapon] || card.weapon;
    if (card.room) return ROOM_NAMES[card.room] || card.room;
    if (card.name) return CHARACTER_NAMES[card.name] || WEAPON_NAMES[card.name] || ROOM_NAMES[card.name] || card.name;
    return 'Carta';
};

export const getCardTypeName = (card) => {
    if (!card) return 'DESCONOCIDA';
    const type = card.cardType || card.type || '';
    if (type === 'CHARACTER' || card.character) return 'Sospechoso';
    if (type === 'WEAPON' || card.weapon) return 'Arma';
    if (type === 'ROOM' || card.room) return 'Habitación';
    return type;
};

export const getCardClass = (card) => {
    const type = card?.cardType || '';
    if (type === 'CHARACTER') return 'card-character';
    if (type === 'WEAPON') return 'card-weapon';
    if (type === 'ROOM') return 'card-room';
    return '';
};

export const BONUS_CARD_INFO = {
    PEEK_CARD: {
        name: 'Echa un vistazo',
        description: 'Cuando alguien muestre una carta para refutar una sugerencia, puedes usar esta carta para verla también. Guárdala hasta que la necesites.',
        image: '/images/cartasbonus/echaunvistazo.png',
    },
    EXTRA_DICE_6: {
        name: 'Suma 6',
        description: 'Añade 6 a tu tirada de movimiento actual. Si ya te moviste, te permite hacer ahora un movimiento adicional de hasta 6 casillas.',
        image: '/images/cartasbonus/suma6.png',
    },
    EXTRA_TURN: {
        name: 'Turno extra',
        description: 'Jugarla finaliza automáticamente tu turno y te permite tirar los dados de nuevo sin esperar. También puedes guardarla.',
        image: '/images/cartasbonus/turnoextra.png',
    },
    TELEPORT_ANY_ROOM: {
        name: 'Muévete a cualquier habitación',
        description: 'Elige una habitación y mueve allí tu peón inmediatamente, sin tirar los dados.',
        image: '/images/cartasbonus/mover.png',
    },
    EXTRA_QUESTION: {
        name: 'Pregunta extra',
        description: 'Formula ahora una pregunta sobre cualquier sospechoso, arma y habitación. No moverás tu peón ni las fichas del sospechoso o del arma.',
        image: '/images/cartasbonus/preguntaextra.png',
    },
    REVEAL_CARD: {
        name: 'Haz que alguien revele una carta',
        description: 'Elige a otro jugador. Tendrá que elegir una carta de su mano y mostrarla a todos inmediatamente.',
        image: '/images/cartasbonus/Revelar.png',
    },
    SKIP_REFUTATION: {
        name: 'Evitar refutación',
        description: 'Solo se puede usar durante una fase de refutación.',
    },
};

export const getBonusCardInfo = (card) =>
    BONUS_CARD_INFO[card?.effect] || { name: 'Carta bonus', description: 'Efecto desconocido.' };

export const cardMatchesSuggestion = (card, suggestion) => {
    if (!card || !suggestion) return false;
    return (card.character && card.character === suggestion.character)
        || (card.weapon && card.weapon === suggestion.weapon)
        || (card.room && card.room === suggestion.room);
};
