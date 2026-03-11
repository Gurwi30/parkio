package it.parkio.app.event;

import it.parkio.app.event.base.Event;

/**
 * Evento emesso quando un parcheggio viene deselezionato.
 *
 * <p>È utile quando l'interfaccia deve tornare a uno stato neutro:
 * per esempio chiudere o svuotare pannelli di dettaglio,
 * rimuovere selezioni visive o aggiornare controlli legati
 * al parcheggio precedentemente selezionato.</p>
 */
public record ParkingLotDeselectEvent() implements Event {
}
