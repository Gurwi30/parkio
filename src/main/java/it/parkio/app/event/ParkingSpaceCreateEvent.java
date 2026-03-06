package it.parkio.app.event;

import it.parkio.app.event.base.Event;
import it.parkio.app.model.ParkingSpace;

public record ParkingSpaceCreateEvent(ParkingSpace space) implements Event {
}
