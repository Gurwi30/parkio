package it.parkio.app.event;

import it.parkio.app.event.base.Event;
import it.parkio.app.model.ParkingSpace;

/**
 * Evento emesso quando un posto auto viene rimosso.
 *
 * <p>Serve per avvisare il resto dell'applicazione che uno spazio
 * non è più disponibile nella struttura dati corrente.
 * I componenti che mostrano informazioni sugli spazi possono così
 * aggiornarsi automaticamente.</p>
 *
 * @param removedSpace spazio che è stato eliminato
 */
public record ParkingSpaceRemoveEvent(ParkingSpace removedSpace) implements Event {
}
