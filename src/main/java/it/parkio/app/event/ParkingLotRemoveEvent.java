package it.parkio.app.event;

import it.parkio.app.event.base.Event;
import it.parkio.app.model.ParkingLot;

/**
 * Evento emesso quando un parcheggio viene rimosso.
 *
 * <p>Permette ai componenti interessati di reagire subito alla rimozione,
 * ad esempio aggiornando l'elenco dei parcheggi, pulendo la selezione corrente
 * oppure ridisegnando la mappa senza l'area eliminata.</p>
 *
 * @param removedParkingLot parcheggio che è stato rimosso
 */
public record ParkingLotRemoveEvent(ParkingLot removedParkingLot) implements Event {

}
