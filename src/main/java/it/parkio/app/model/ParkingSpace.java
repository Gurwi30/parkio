package it.parkio.app.model;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import java.time.Instant;

public class ParkingSpace {

    public static final JsonDeserializer<ParkingSpace> DESERIALIZER = (json, _, ctx) -> {
        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        Bounds bounds = ctx.deserialize(jsonObject.get("bounds"), Bounds.class);
        ParkingSpace.Type type = ctx.deserialize(jsonObject.get("type"), ParkingSpace.Type.class);

        String statusIdentifier = jsonObject.get("status").getAsString();

        ParkingSpaceStatus status = switch (statusIdentifier.toLowerCase()) {

            case "occupied" -> {
                JsonObject occupiedData = jsonObject.getAsJsonObject("occupied");

                String carPlate = occupiedData.get("carPlate").getAsString();
                Instant start = Instant.parse(occupiedData.get("start").getAsString());

                if (occupiedData.has("end")) {
                    yield ParkingSpaceStatus.occupied(carPlate, start, Instant.parse(occupiedData.get("end").getAsString()));
                }

                yield ParkingSpaceStatus.occupied(carPlate, start);
            }

            case "reserved" -> {
                JsonObject reservedData = jsonObject.getAsJsonObject("reserved");

                String carPlate = reservedData.get("carPlate").getAsString();
                Instant start = Instant.parse(reservedData.get("start").getAsString());
                Instant end = Instant.parse(reservedData.get("end").getAsString());

                yield ParkingSpaceStatus.reserved(carPlate, start, end);
            }

            default -> ParkingSpaceStatus.free();
        };

        return new ParkingSpace(id, null, bounds, type, status);
    };

    public static final JsonSerializer<ParkingSpace> SERIALIZER = (parkingSpace, _, ctx) -> {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", parkingSpace.id);
        jsonObject.add("bounds", ctx.serialize(parkingSpace.bounds));
        jsonObject.addProperty("type", parkingSpace.type.name());
        jsonObject.addProperty("status", parkingSpace.status.getIdentifier());

        JsonObject statusData = switch (parkingSpace.status) {
            case ParkingSpaceStatus.Occupied occupied -> {
                JsonObject occupiedData = new JsonObject();

                occupiedData.addProperty("carPlate", occupied.getCarPlate());
                occupiedData.addProperty("start", occupied.getStart().toString());

                occupied.getEnd().ifPresent(end -> occupiedData.addProperty("end", end.toString()));

                yield occupiedData;
            }

            case ParkingSpaceStatus.Reserved reserved -> {
                JsonObject reservedData = new JsonObject();

                reservedData.addProperty("carPlate", reserved.getCarPlate());
                reservedData.addProperty("start", reserved.getStart().toString());
                reservedData.addProperty("end", reserved.getEnd().toString());

                yield reservedData;
            }

            default -> null;
        };

        if (statusData != null) jsonObject.add(parkingSpace.getStatus().getIdentifier().toLowerCase(), statusData);

        return jsonObject;
    };

    private final int id;
    protected final ParkingLot parkingLot;
    private final Bounds bounds;
    private final Type type;
    private ParkingSpaceStatus status;

    public ParkingSpace(int id, ParkingLot parkingLot, Bounds bounds, Type type, ParkingSpaceStatus status) {
        this.id = id;
        this.parkingLot = parkingLot;
        this.bounds = bounds;
        this.type = type;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public Type getType() {
        return type;
    }

    public ParkingSpaceStatus getStatus() {
        return status;
    }

    public void updateStatus(ParkingSpaceStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("ParkingSpace { id: %d, bounds: %s, status: %s }", id, bounds, status);
    }

    public enum Type {
        NORMAL,
        ELECTRIC,
        HANDICAPPED
    }

}
