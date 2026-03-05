package it.parkio.app.model;

import com.google.gson.*;
import it.parkio.app.util.ColorUtil;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ParkingLot {

    public static final JsonDeserializer<ParkingLot> DESERIALIZER = (json, _, ctx) -> {
        if (!json.isJsonObject()) throw new JsonParseException("Expected a JsonObject");

        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        String name = jsonObject.get("name").getAsString();
        String colorStr = jsonObject.get("color").getAsString();
        Color color = Color.decode(colorStr);

        Bounds bounds = ctx.deserialize(jsonObject.get("bounds"), Bounds.class);

        List<ParkingSpace> spaces = jsonObject.getAsJsonArray("spaces")
                .asList().stream()
                .map(element -> (ParkingSpace) ctx.deserialize(element, ParkingSpace.class))
                .toList();

        ParkingLot parkingLot = new ParkingLot(id, bounds, name, color);
        spaces.forEach(parkingSpace -> {
            parkingSpace.parkingLot = parkingLot;
            parkingLot.spaces.put(parkingSpace.getId(), parkingSpace);
        });

        return parkingLot;
    };

    public static final JsonSerializer<ParkingLot> SERIALIZER = (parkingLot, _, ctx) -> { // CREA OGGETTO DA SCRIVERE NEL FILE JSON
        JsonObject jsonObject = new JsonObject();
        JsonArray spaces = new JsonArray();

        parkingLot.getSpaces().forEach(space -> spaces.add(ctx.serialize(space)));

        jsonObject.addProperty("id", parkingLot.getId());
        jsonObject.add("bounds", ctx.serialize(parkingLot.getBounds()));
        jsonObject.addProperty("name", parkingLot.getName());
        jsonObject.addProperty("color", ColorUtil.toHexString(parkingLot.getColor()));
        jsonObject.add("spaces", spaces);

        return jsonObject;
    };

    private final Map<Integer, ParkingSpace> spaces = new HashMap<>();

    private final int id;

    private  Bounds bounds;
    private String name;
    private Color color;

    public ParkingLot(int id, Bounds bounds, String name, Color color) {
        this.id = id;
        this.bounds = bounds;
        this.name = name;
        this.color = color;
    }

    public ParkingSpace addParkingSpace(Bounds bounds, ParkingSpace.Type type) {
        int id = getNextAvailableSpaceId();
        ParkingSpace parkingSpace = new ParkingSpace(id, this, bounds, type, ParkingSpaceStatus.free());

        spaces.put(id, parkingSpace);
        return parkingSpace;
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

    public Bounds getBounds() {
        return bounds;
    }

    public void updateBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private int getNextAvailableSpaceId() {
        return spaces.size() + 1;
    }

    @Override
    public String toString() {
        return String.format("ParkingLot { id: %d, bounds: %s, name: '%s', color: %s }", id, bounds, name, ColorUtil.toHexString(color));
    }

}
