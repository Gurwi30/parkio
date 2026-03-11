package it.parkio.app.event;

import it.parkio.app.event.base.Event;
import it.parkio.app.model.ParkingLot;

/**
 * Evento emesso quando viene creato un nuovo parcheggio.
 *
 * <p>Serve per notificare alle varie parti dell'applicazione
 * che un nuovo {@link ParkingLot} è stato aggiunto.
 * Ad esempio, la UI può ascoltare questo evento per aggiornare
 * automaticamente liste, pannelli o la mappa.</p>
 *
 * @param parkingLot parcheggio appena creato
 */
public record ParkingLotCreateEvent(ParkingLot parkingLot) implements Event {

}
