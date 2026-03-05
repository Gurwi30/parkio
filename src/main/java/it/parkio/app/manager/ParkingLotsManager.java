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

    private static final Gson GSON = new GsonBuilder() // CREA SCRITTORE FILE JSON CON TUTTI I TIPI CUSTOM
            .registerTypeAdapter(Bounds.class, Bounds.DESERIALIZER) // SPECIFICA COME CONVERTIRE IL CONTENUTO JSON NELL' OGGETTO ORIGINARIO
            .registerTypeAdapter(Bounds.class, Bounds.SERIALIZER)  // SPECIFICA COME SCRIVERE L' OGGETO NEL FILE JSON
            .registerTypeAdapter(GeoPosition.class, JsonTypeAdapters.GEO_POSITION_DESERIALIZER)
            .registerTypeAdapter(GeoPosition.class, JsonTypeAdapters.GEO_POSITION_SERIALIZER)
            .registerTypeAdapter(ParkingLot.class, ParkingLot.DESERIALIZER)
            .registerTypeAdapter(ParkingLot.class, ParkingLot.SERIALIZER)
            .registerTypeAdapter(ParkingSpace.class, ParkingSpace.DESERIALIZER)
            .registerTypeAdapter(ParkingSpace.class, ParkingSpace.SERIALIZER)
            .setFormattingStyle(FormattingStyle.PRETTY.withIndent("    ")) // SPECIFICATA INDENTAZIONE FILE A 4 SPAZI
            .create();

    private final Map<Integer, ParkingLot> parkingLots = new HashMap<>(); // DIZIONARIO CHIAVE VALORE PER L'ID DELL'PARCHEGGIO

    public static @NotNull ParkingLotsManager load(@NotNull File file) throws IOException { // CREA IL MANAGER A PARTIRE DA UN FILE .json
        if (!file.exists()) throw new IOException("Parking lots file not found: " + file.getAbsolutePath()); // VERIFICA SE ESISTE E SE E' VALIDO
        if (!file.getName().endsWith(".json")) throw new IOException("Parking lots file must be a JSON file: " + file.getAbsolutePath());

        ParkIO.LOGGER.info("Loading parking lots from file: {}", file.getAbsolutePath());

        ParkingLotsManager manager = new ParkingLotsManager(); // CREA IL MANAGER

        try (FileReader reader = new FileReader(file)) { // LEGGE IL FILE
            ParkingLot[] readData = GSON.fromJson(reader, ParkingLot[].class); // ESTRA CONTENUTI DAL JSON

            if (readData != null) {
                for (ParkingLot readLot : readData) {
                    manager.parkingLots.put(readLot.getId(), readLot); // AGGIUNGE I PARCHEGGI LETTI AL MANAGER APPENA CREATO
                }
            }
        } catch (Exception e) {
            ParkIO.LOGGER.error("Error loading parking lots from file: {}", file.getAbsolutePath(), e);
        }

        ParkIO.LOGGER.info("Loaded {} parking lots", manager.getParkingLots().size());

        return manager;
    }

    public ParkingLot createParkingLot(String name, Bounds bounds, Color color) { // CREA UN PARCHEGGIO
        int id = getNextAvailableParkingLotId(); // PRENDE PROSSIMO ID DISPONIBILE

        ParkingLot parkingLot = new ParkingLot(id, bounds, name, color); // CREA IL PARCHEGGIO
        parkingLots.put(id, parkingLot); // AGGIUNGE IL PARCHEGGIO

        return parkingLot; // RITORNA IL PARCHEGGIO CREATO
    }

    public void removeParkingLot(int id) {
        parkingLots.remove(id);
    }

    public Optional<ParkingLot> getParkingLot(int id) {
        return Optional.ofNullable(parkingLots.get(id));
    } // PRENDE PARCHEGGIO A PARTIRE DAL SUO ID

    public @Unmodifiable Set<ParkingLot> getParkingLots() {
        return Set.copyOf(parkingLots.values());
    } // RITORNA LISTA PARCHEGGI NON MODIFICABILE

    private int getNextAvailableParkingLotId() {
        return parkingLots.size() + 1;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void save(@NotNull File file) throws IOException { // SALVA I PARCHEGGI NEL FILE JSON
        if (!file.exists()) {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            file.createNewFile();
        }

        ParkIO.LOGGER.info("Saving {} parking lots to file: {}", parkingLots.size(), file.getAbsolutePath());

        try (FileWriter writer = new FileWriter(file)) { // APRE IL SCRITTORE FILE
            GSON.toJson(parkingLots.values(), writer); // CONVERTE I DATI IN JSON E SCRIVE NEL FILE
        }

        ParkIO.LOGGER.info("Saved parking lots: {}", parkingLots.values());
    }

}
