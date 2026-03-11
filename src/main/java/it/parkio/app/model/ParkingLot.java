package it.parkio.app.model;

import com.google.gson.*;
import it.parkio.app.ParkIO;
import it.parkio.app.event.ParkingSpaceCreateEvent;
import it.parkio.app.event.ParkingSpaceRemoveEvent;
import it.parkio.app.util.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Rappresenta un parcheggio composto da:
 * <ul>
 *     <li>un id univoco;</li>
 *     <li>un'area geografica ({@link Bounds});</li>
 *     <li>un nome descrittivo;</li>
 *     <li>un colore per la visualizzazione;</li>
 *     <li>una collezione di posti auto interni.</li>
 * </ul>
 *
 * <p>Questa classe contiene anche la logica di serializzazione JSON
 * e la gestione base degli spazi associati.</p>
 */
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

            if (parkingSpace.getId() <= parkingLot.nextAvailableSpaceId) parkingLot.nextAvailableSpaceId = parkingSpace.getId() + 1;
        });

        return parkingLot;
    };

    /**
     * Serializzatore JSON del parcheggio.
     *
     * <p>Produce una struttura compatta e leggibile,
     * includendo anche tutti gli spazi contenuti al suo interno.</p>
     */
    public static final JsonSerializer<ParkingLot> SERIALIZER = (parkingLot, _, ctx) -> {
        JsonObject jsonObject = new JsonObject();
        JsonArray spaces = new JsonArray();

        // Serializza uno ad uno tutti gli spazi del parcheggio.
        parkingLot.getSpaces().forEach(space -> spaces.add(ctx.serialize(space)));

        jsonObject.addProperty("id", parkingLot.getId());
        jsonObject.add("bounds", ctx.serialize(parkingLot.getBounds()));
        jsonObject.addProperty("name", parkingLot.getName());
        jsonObject.addProperty("color", ColorUtil.toHexString(parkingLot.getColor()));
        jsonObject.add("spaces", spaces);

        return jsonObject;
    };

    /**
     * Collezione degli spazi del parcheggio, indicizzati per id.
     */
    private final Map<Integer, ParkingSpace> spaces = new HashMap<>();

    /**
     * Prossimo id disponibile per un nuovo posto auto.
     */
    private int nextAvailableSpaceId = 0;

    /**
     * Id univoco del parcheggio.
     */
    private final int id;

    /**
     * Area geografica del parcheggio.
     */
    private Bounds bounds;

    /**
     * Nome mostrato all'utente.
     */
    private String name;

    /**
     * Colore usato nella mappa e in alcuni componenti grafici.
     */
    private Color color;

    /**
     * Crea un nuovo parcheggio.
     *
     * @param id     identificativo univoco
     * @param bounds area geografica del parcheggio
     * @param name   nome descrittivo
     * @param color  colore associato
     */
    public ParkingLot(int id, Bounds bounds, String name, Color color) {
        this.id = id;
        this.bounds = bounds;
        this.name = name;
        this.color = color;
    }

    /**
     * Aggiunge un nuovo posto auto al parcheggio.
     *
     * <p>Ogni nuovo spazio parte inizialmente come libero.</p>
     *
     * @param bounds area del posto auto
     * @param type   tipologia del posto
     * @return spazio appena creato
     */
    public ParkingSpace addParkingSpace(Bounds bounds, ParkingSpace.Type type) {
        int id = getNextAvailableSpaceId();
        ParkingSpace parkingSpace = new ParkingSpace(id, this, bounds, type, ParkingSpaceStatus.free());

        spaces.put(id, parkingSpace);

        // Notifica ai componenti interessati che è stato creato un nuovo spazio.
        ParkIO.EVENT_MANAGER.call(new ParkingSpaceCreateEvent(parkingSpace));

        return parkingSpace;
    }

    /**
     * Rimuove uno spazio tramite id.
     *
     * @param id id dello spazio da eliminare
     */
    public void removeSpace(int id) {
        ParkingSpace parkingSpace = spaces.get(id);
        if (parkingSpace == null) return;

        spaces.remove(id);
        ParkIO.EVENT_MANAGER.call(new ParkingSpaceRemoveEvent(parkingSpace));
    }

    /**
     * Variante comoda per rimuovere direttamente l'oggetto spazio.
     *
     * @param space spazio da rimuovere
     */
    public void removeSpace(@NotNull ParkingSpace space) {
        removeSpace(space.getId());
    }

    /**
     * Cerca uno spazio per id.
     *
     * @param id identificativo dello spazio
     * @return {@code Optional} con lo spazio, se esiste
     */
    public Optional<ParkingSpace> getSpace(int id) {
        return Optional.ofNullable(spaces.get(id));
    }

    /**
     * Restituisce una copia immutabile degli spazi del parcheggio.
     *
     * @return lista non modificabile degli spazi
     */
    public @Unmodifiable List<ParkingSpace> getSpaces() {
        return List.copyOf(spaces.values());
    }

    /**
     * @return id del parcheggio
     */
    public int getId() {
        return id;
    }

    /**
     * @return bounds correnti del parcheggio
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Aggiorna l'area geografica del parcheggio.
     *
     * @param bounds nuovi bounds
     */
    public void updateBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    /**
     * @return nome del parcheggio
     */
    public String getName() {
        return name;
    }

    /**
     * Aggiorna il nome del parcheggio.
     *
     * @param name nuovo nome
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return colore associato al parcheggio
     */
    public Color getColor() {
        return color;
    }

    /**
     * Aggiorna il colore del parcheggio.
     *
     * @param color nuovo colore
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Genera il prossimo id disponibile per uno spazio interno.
     *
     * @return nuovo id univoco per il posto auto
     */
    private int getNextAvailableSpaceId() {
        return nextAvailableSpaceId++;
    }

    /**
     * Rappresentazione testuale utile in log e debug.
     */
    @Override
    public String toString() {
        return String.format("ParkingLot { id: %d, bounds: %s, name: '%s', color: %s }", id, bounds, name, ColorUtil.toHexString(color));
    }

}
