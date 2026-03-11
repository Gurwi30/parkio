package it.parkio.app.manager;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.parkio.app.ParkIO;
import it.parkio.app.event.ParkingLotCreateEvent;
import it.parkio.app.event.ParkingLotRemoveEvent;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gestisce l'insieme dei parcheggi presenti nell'applicazione.
 *
 * <p>Questa classe ha tre responsabilità principali:</p>
 * <ul>
 *     <li>mantenere in memoria tutti i parcheggi caricati o creati;</li>
 *     <li>fornire operazioni di creazione, ricerca e rimozione;</li>
 *     <li>salvare e caricare i dati dal file JSON persistente.</li>
 * </ul>
 *
 * <p>Inoltre pubblica eventi quando un parcheggio viene creato o rimosso,
 * così la UI e gli altri componenti possono aggiornarsi automaticamente.</p>
 */
public class ParkingLotsManager {

    /**
     * Istanza Gson configurata con tutti gli adapter necessari per serializzare
     * e deserializzare correttamente i tipi del dominio applicativo.
     *
     * <p>La formattazione "pretty" rende il file JSON più leggibile
     * anche a occhio umano.</p>
     */
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

    /**
     * Mappa dei parcheggi indicizzati per id.
     *
     * <p>Usare una mappa permette accessi veloci per id
     * e semplifica operazioni di aggiunta e rimozione.</p>
     */
    private final Map<Integer, ParkingLot> parkingLots = new HashMap<>();

    /**
     * Tiene traccia del prossimo id disponibile da assegnare a un nuovo parcheggio.
     */
    private int nextAvailableParkingLotId = 0;

    /**
     * Carica i parcheggi da un file JSON.
     *
     * <p>Se il file non esiste o non è un file JSON valido,
     * viene sollevata un'eccezione. Gli errori di parsing interni
     * vengono comunque loggati per aiutare il debug.</p>
     *
     * @param file file da cui leggere i dati
     * @return gestore inizializzato con i parcheggi letti
     * @throws IOException se il file non esiste o non è valido come input
     */
    public static @NotNull ParkingLotsManager load(@NotNull File file) throws IOException {
        if (!file.exists()) throw new IOException("Parking lots file not found: " + file.getAbsolutePath());
        if (!file.getName().endsWith(".json")) throw new IOException("Parking lots file must be a JSON file: " + file.getAbsolutePath());

        ParkIO.LOGGER.info("Loading parking lots from file: {}", file.getAbsolutePath());

        ParkingLotsManager manager = new ParkingLotsManager();

        try (FileReader reader = new FileReader(file)) {
            ParkingLot[] readData = GSON.fromJson(reader, ParkingLot[].class);

            if (readData != null) {
                for (ParkingLot readLot : readData) {
                    // Inserisce ogni parcheggio caricato nella mappa interna.
                    manager.parkingLots.put(readLot.getId(), readLot);

                    // Aggiorna il prossimo id disponibile per evitare collisioni.
                    if (readLot.getId() <= manager.nextAvailableParkingLotId) manager.nextAvailableParkingLotId = readLot.getId() + 1;
                }
            }
        } catch (Exception e) {
            ParkIO.LOGGER.error("Error loading parking lots from file: {}", file.getAbsolutePath(), e);
        }

        ParkIO.LOGGER.info("Loaded {} parking lots", manager.getParkingLots().size());

        return manager;
    }

    /**
     * Crea un nuovo parcheggio, lo aggiunge alla struttura interna
     * e notifica l'evento di creazione.
     *
     * @param name   nome del parcheggio
     * @param bounds area geografica occupata dal parcheggio
     * @param color  colore associato al parcheggio nella UI
     * @return parcheggio appena creato
     */
    public ParkingLot createParkingLot(String name, Bounds bounds, Color color) {
        int id = getNextAvailableParkingLotId();

        ParkingLot parkingLot = new ParkingLot(id, bounds, name, color);
        parkingLots.put(id, parkingLot);

        // Notifica il resto dell'applicazione che un nuovo parcheggio esiste.
        ParkIO.EVENT_MANAGER.call(new ParkingLotCreateEvent(parkingLot));

        return parkingLot;
    }

    /**
     * Rimuove un parcheggio tramite il suo id.
     *
     * <p>Se l'id non esiste, il metodo termina senza fare nulla,
     * evitando errori inutili.</p>
     *
     * @param id id del parcheggio da rimuovere
     */
    public void removeParkingLot(int id) {
        ParkingLot parkingLot = parkingLots.get(id);
        if (parkingLot == null) return;

        parkingLots.remove(id);

        // Pubblica l'evento di rimozione per aggiornare UI e altri componenti.
        ParkIO.EVENT_MANAGER.call(new ParkingLotRemoveEvent(parkingLot));
    }

    /**
     * Comodità per rimuovere un parcheggio passando direttamente l'oggetto.
     *
     * @param parkingLot parcheggio da rimuovere
     */
    public void removeParkingLot(@NotNull ParkingLot parkingLot) {
        removeParkingLot(parkingLot.getId());
    }

    /**
     * Cerca un parcheggio per id.
     *
     * @param id identificativo del parcheggio
     * @return {@code Optional} contenente il parcheggio, se trovato
     */
    public Optional<ParkingLot> getParkingLot(int id) {
        return Optional.ofNullable(parkingLots.get(id));
    }

    /**
     * Restituisce una lista non modificabile dei parcheggi attualmente presenti.
     *
     * <p>Viene restituita una copia per evitare modifiche esterne accidentali
     * alla struttura interna del manager.</p>
     *
     * @return lista immutabile dei parcheggi
     */
    public @Unmodifiable List<ParkingLot> getParkingLots() {
        return List.copyOf(parkingLots.values());
    }

    /**
     * Genera il prossimo id disponibile e incrementa il contatore interno.
     *
     * @return nuovo id univoco per un parcheggio
     */
    private int getNextAvailableParkingLotId() {
        return nextAvailableParkingLotId++;
    }

    /**
     * Salva tutti i parcheggi correnti nel file indicato.
     *
     * <p>Se il file non esiste viene creato automaticamente,
     * insieme alle eventuali cartelle parent.</p>
     *
     * @param file file di destinazione
     * @throws IOException se avviene un errore durante la scrittura
     */
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
