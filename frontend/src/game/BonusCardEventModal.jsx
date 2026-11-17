import { BONUS_CARD_INFO, getBonusCardInfo } from './gameConstants';

export default function BonusCardEventModal({
    card,
    onPlay,
    onWait,
    isPlaying,
    canPlay,
    rooms,
    characters,
    weapons,
    players,
    selectedRoom,
    onRoomChange,
    selectedCharacter,
    onCharacterChange,
    selectedWeapon,
    onWeaponChange,
    selectedTarget,
    onTargetChange,
}) {
    if (!card) return null;
    const immediate = card.immediate;
    const playableWhenDrawn = [
        'EXTRA_DICE_6',
        'TELEPORT_ANY_ROOM',
        'EXTRA_QUESTION',
        'REVEAL_CARD',
        'EXTRA_TURN',
    ].includes(card.effect);

    return (
        <div className="solution-notice-backdrop" role="presentation">
            <section className="solution-notice bonus-card-notice" role="dialog" aria-modal="true" aria-labelledby="bonus-card-title">
                <p className="solution-notice-eyebrow">¡HAS CAÍDO EN UNA CASILLA BONUS!</p>
                <h2 id="bonus-card-title">{getBonusCardInfo(card).name}</h2>
                {BONUS_CARD_INFO[card.effect]?.image && (
                    <img
                        className="bonus-card-event-image"
                        src={BONUS_CARD_INFO[card.effect].image}
                        alt=""
                    />
                )}
                {!card.unavailable && <p>{getBonusCardInfo(card).description}</p>}
                {!card.unavailable && card.effect === 'EXTRA_DICE_6' && (
                    <p className="gameplay-bonus-status">
                        El +6 se suma a tu tirada actual. Si ya te moviste, tendrás ahora un movimiento adicional de hasta 6 casillas.
                    </p>
                )}
                <p className="solution-notice-deal">
                    {card.unavailable
                        ? 'No quedan cartas en el mazo bonus.'
                        : immediate
                            ? 'Esta carta debe jugarse ahora.'
                            : 'La carta se ha añadido a tu mano. Puedes guardarla hasta el momento indicado.'}
                </p>
                {!card.unavailable && card.effect === 'TELEPORT_ANY_ROOM' && (
                    <label className="bonus-card-event-field">
                        Habitación de destino
                        <select value={selectedRoom} onChange={(event) => onRoomChange(event.target.value)}>
                            <option value="">Elige una habitación</option>
                            {rooms.map(([room, name]) => <option key={room} value={room}>{name}</option>)}
                        </select>
                    </label>
                )}
                {!card.unavailable && card.effect === 'EXTRA_QUESTION' && (
                    <div className="bonus-card-event-fields">
                        <label className="bonus-card-event-field">
                            Sospechoso
                            <select value={selectedCharacter} onChange={(event) => onCharacterChange(event.target.value)}>
                                <option value="">Elige sospechoso</option>
                                {characters.map(([character, name]) => <option key={character} value={character}>{name}</option>)}
                            </select>
                        </label>
                        <label className="bonus-card-event-field">
                            Arma
                            <select value={selectedWeapon} onChange={(event) => onWeaponChange(event.target.value)}>
                                <option value="">Elige arma</option>
                                {weapons.map(([weapon, name]) => <option key={weapon} value={weapon}>{name}</option>)}
                            </select>
                        </label>
                        <label className="bonus-card-event-field">
                            Habitación
                            <select value={selectedRoom} onChange={(event) => onRoomChange(event.target.value)}>
                                <option value="">Elige habitación</option>
                                {rooms.map(([room, name]) => <option key={room} value={room}>{name}</option>)}
                            </select>
                        </label>
                    </div>
                )}
                {!card.unavailable && card.effect === 'REVEAL_CARD' && (
                    <label className="bonus-card-event-field">
                        Jugador que debe revelar una carta
                        <select value={selectedTarget} onChange={(event) => onTargetChange(event.target.value)}>
                            <option value="">Elige jugador</option>
                            {players.map((player) => (
                                <option key={player.id} value={player.id}>{player.user?.username || player.name}</option>
                            ))}
                        </select>
                    </label>
                )}
                <div className="bonus-card-event-actions">
                    {playableWhenDrawn && !card.unavailable && (
                        <button
                            type="button"
                            className="gameplay-roll-button"
                            onClick={onPlay}
                            disabled={!canPlay || isPlaying}
                        >
                            {isPlaying ? 'Jugando...' : 'Jugar ahora'}
                        </button>
                    )}
                    {!immediate && !card.unavailable && (
                        <button
                            type="button"
                            className="gameplay-bonus-use-button"
                            onClick={onWait}
                            disabled={isPlaying}
                        >
                            Guardar carta
                        </button>
                    )}
                    {card.unavailable && (
                        <button type="button" className="gameplay-bonus-use-button" onClick={onWait}>
                            Continuar
                        </button>
                    )}
                </div>
                {!card.unavailable && !playableWhenDrawn && (
                    <p className="gameplay-bonus-status">
                        {card.effect === 'PEEK_CARD'
                            ? 'Podrás usarla después de que otro jugador muestre una carta.'
                            : card.effect === 'EXTRA_DICE_6'
                                ? 'Puedes jugarla para sumar 6 a la tirada actual o conseguir un movimiento adicional si ya te moviste.'
                                : 'Podrás usarla al inicio de tu próximo turno, antes de tirar los dados.'}
                    </p>
                )}
            </section>
        </div>
    );
}
