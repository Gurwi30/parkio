package it.parkio.app.event;

import it.parkio.app.event.base.Event;
import it.parkio.app.model.ParkingLot;

/**
 * Evento emesso quando un parcheggio viene selezionato.
 *
 * <p>Viene usato per sincronizzare le varie parti dell'interfaccia:
 * per esempio un click sulla mappa può produrre questo evento,
 * e il pannello laterale può ascoltarlo per mostrare i dettagli
 * del parcheggio selezionato.</p>
 *
 * @param selectedParkingLot parcheggio selezionato dall'utente
 */
public record ParkingLotSelectEvent(ParkingLot selectedParkingLot) implements Event {

}
