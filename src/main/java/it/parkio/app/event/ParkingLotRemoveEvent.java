package it.parkio.app.event;

import it.parkio.app.event.base.Event;
import it.parkio.app.model.ParkingLot;

public record ParkingLotRemoveEvent(ParkingLot removedParkingLot) implements Event {

}
