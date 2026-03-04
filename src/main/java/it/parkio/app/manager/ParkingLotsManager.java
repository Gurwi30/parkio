package it.parkio.app.manager;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.parkio.app.ParkIO;
import it.parkio.app.json.JsonTypeAdapters;
import it.parkio.app.model.Bounds;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.ParkingSpace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ParkingLotsManager {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Bounds.class, Bounds.DESERIALIZER)
            .registerTypeAdapter(Bounds.class, Bounds.SERIALIZER)
            .registerTypeAdapter(GeoPosition.class, JsonTypeAdapters.GEO_POSITION_DESERIALIZER)
            .registerTypeAdapter(GeoPosition.class, JsonTypeAdapters.GEO_POSITION_SERIALIZER)
            .registerTypeAdapter(ParkingLot.class, ParkingLot.DESERIALIZER)
            .registerTypeAdapter(ParkingLot.class, ParkingLot.SERIALIZER)
            .registerTypeAdapter(ParkingSpace.class, ParkingSpace.DESERIALIZER)
            .registerTypeAdapter(ParkingSpace.class, ParkingSpace.SERIALIZER)
            .setFormattingStyle(FormattingStyle.PRETTY.withIndent("    "))
            .create();

    private final Map<Integer, ParkingLot> parkingLots = new HashMap<>();

    public static @NotNull ParkingLotsManager load(@NotNull File file) throws IOException {
        if (!file.exists()) throw new IOException("Parking lots file not found: " + file.getAbsolutePath());
        if (!file.getName().endsWith(".json")) throw new IOException("Parking lots file must be a JSON file: " + file.getAbsolutePath());

        ParkIO.LOGGER.info("Loading parking lots from file: {}", file.getAbsolutePath());

        ParkingLotsManager manager = new ParkingLotsManager();

        try (FileReader reader = new FileReader(file)) {
            ParkingLot[] readData = GSON.fromJson(reader, ParkingLot[].class);

            if (readData != null) {
                for (ParkingLot readLot : readData) {
                    manager.parkingLots.put(readLot.getId(), readLot);
                }
            }
        } catch (Exception e) {
            ParkIO.LOGGER.error("Error loading parking lots from file: {}", file.getAbsolutePath(), e);
        }

        ParkIO.LOGGER.info("Loaded {} parking lots", manager.getParkingLots().size());

        return manager;
    }

    public ParkingLot createParkingLot(String name, Bounds bounds, Color color) {
        int id = getNextAvailableParkingLotId();
        ParkingLot parkingLot = new ParkingLot(id, bounds, name, color);
        parkingLots.put(id, parkingLot);

        return parkingLot;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void save(@NotNull File file) throws IOException {
        if (!file.exists()) {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            file.createNewFile();
        }

        ParkIO.LOGGER.info("Saving {} parking lots to file: {}", parkingLots.size(), file.getAbsolutePath());

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(parkingLots.values(), writer);
        }

        ParkIO.LOGGER.info("Saved parking lots: {}", parkingLots.values());
    }

}
