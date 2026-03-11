package it.parkio.app.event;

import it.parkio.app.event.base.Event;
import it.parkio.app.model.ParkingSpace;

/**
 * Evento emesso quando viene creato un nuovo posto auto all'interno di un parcheggio.
 *
 * <p>Questo evento consente di aggiornare in modo reattivo la UI o altre logiche
 * applicative senza accoppiare direttamente i componenti tra loro.
 * Chi ascolta l'evento può, per esempio, aggiornare una lista degli spazi
 * o forzare un ridisegno della mappa.</p>
 *
 * @param space spazio di parcheggio appena creato
 */
public record ParkingSpaceCreateEvent(ParkingSpace space) implements Event {
}
