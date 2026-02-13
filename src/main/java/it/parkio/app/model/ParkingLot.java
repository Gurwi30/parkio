package it.parkio.app.model;

import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.util.*;

public class ParkingLot {

    private final Map<Integer, ParkingSpace> spaces = new HashMap<>();

    private final int id;
    private final String name;
    private final Color color;

    public ParkingLot(int id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public void addSpace() {
        int id = getNextAvailableSpaceId();

        spaces.put(id, new ParkingSpace(id, this));
    }

    public void removeSpace(int id) {
        spaces.remove(id);
    }

    public Optional<ParkingSpace> getSpace(int id) {
        return Optional.ofNullable(spaces.get(id));
    }

    public @Unmodifiable Set<ParkingSpace> getSpaces() {
        return Set.copyOf(spaces.values());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    private int getNextAvailableSpaceId() {
        return spaces.size() + 1;
    }

}
