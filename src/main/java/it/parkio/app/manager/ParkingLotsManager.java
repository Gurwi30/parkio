package it.parkio.app.manager;

import it.parkio.app.model.ParkingLot;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ParkingLotsManager {

    private final Map<Integer, ParkingLot> parkingLots = new HashMap<>();

    public void createParkingLot(String name, Color color) {
        int id = getNextAvailableParkingLotId();
        parkingLots.put(id, new ParkingLot(id, name, color));
    }

    public void removeParkingLot(int id) {
        parkingLots.remove(id);
    }

    public Optional<ParkingLot> getParkingLot(int id) {
        return Optional.ofNullable(parkingLots.get(id));
    }

    public @Unmodifiable Set<ParkingLot> getParkingLots() {
        return Set.copyOf(parkingLots.values());
    }

    private int getNextAvailableParkingLotId() {
        return parkingLots.size() + 1;
    }

}
