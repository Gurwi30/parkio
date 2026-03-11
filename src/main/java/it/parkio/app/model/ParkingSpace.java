package it.parkio.app.model;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import java.time.Instant;

/**
 * Rappresenta un singolo posto auto all'interno di un parcheggio.
 *
 * <p>Ogni spazio possiede:</p>
 * <ul>
 *     <li>un id univoco nel proprio parcheggio;</li>
 *     <li>un riferimento al parcheggio padre;</li>
 *     <li>un'area geografica;</li>
 *     <li>una tipologia (normale, elettrico, disabili);</li>
 *     <li>uno stato corrente (libero, occupato o riservato).</li>
 * </ul>
 */
public class ParkingSpace {

    /**
     * Deserializzatore JSON del posto auto.
     *
     * <p>Ricostruisce lo spazio leggendo anche il suo stato specifico.
     * Alcuni stati richiedono campi extra, come targa e date.</p>
     *
     * <p>Il riferimento al parcheggio padre non viene assegnato qui,
     * ma successivamente durante la ricostruzione del {@link ParkingLot}.</p>
     */
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

    /**
     * Serializzatore JSON del posto auto.
     *
     * <p>Salva sempre le informazioni base dello spazio
     * e aggiunge i dettagli extra solo quando lo stato lo richiede.</p>
     */
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

        // I dati aggiuntivi vengono scritti sotto una chiave coerente con lo stato.
        if (statusData != null) jsonObject.add(parkingSpace.getStatus().getIdentifier().toLowerCase(), statusData);

        return jsonObject;
    };

    /**
     * Id del posto auto.
     */
    private final int id;

    /**
     * Riferimento al parcheggio che contiene questo spazio.
     *
     * <p>È {@code protected} perché deve essere ricollegato in fase di deserializzazione
     * dalla classe {@link ParkingLot}.</p>
     */
    protected ParkingLot parkingLot;

    /**
     * Area geografica del posto auto.
     */
    private Bounds bounds;

    /**
     * Tipologia del posto auto.
     */
    private Type type;

    /**
     * Stato corrente del posto auto.
     */
    private ParkingSpaceStatus status;

    /**
     * Costruttore completo.
     *
     * @param id         id dello spazio
     * @param parkingLot parcheggio di appartenenza
     * @param bounds     area geografica
     * @param type       tipologia
     * @param status     stato iniziale
     */
    public ParkingSpace(int id, ParkingLot parkingLot, Bounds bounds, Type type, ParkingSpaceStatus status) {
        this.id = id;
        this.parkingLot = parkingLot;
        this.bounds = bounds;
        this.type = type;
        this.status = status;
    }

    /**
     * @return id dello spazio
     */
    public int getId() {
        return id;
    }

    /**
     * @return parcheggio padre dello spazio
     */
    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    /**
     * @return bounds geografici dello spazio
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Aggiorna l'area geografica dello spazio.
     *
     * @param bounds nuovi bounds
     */
    public void updateBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    /**
     * @return tipo dello spazio
     */
    public Type getType() {
        return type;
    }

    /**
     * Modifica il tipo dello spazio.
     *
     * @param type nuovo tipo
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return stato corrente dello spazio
     */
    public ParkingSpaceStatus getStatus() {
        return status;
    }

    /**
     * Aggiorna lo stato dello spazio.
     *
     * <p>Il metodo modifica solo il dato in memoria;
     * eventuali effetti temporali o repaint devono essere gestiti altrove.</p>
     *
     * @param status nuovo stato
     */
    public void updateStatus(ParkingSpaceStatus status) {
        this.status = status;
    }

    /**
     * Rappresentazione testuale utile per log e debug.
     */
    @Override
    public String toString() {
        return String.format("ParkingSpace { id: %d, bounds: %s, status: %s }", id, bounds, status);
    }

    /**
     * Tipologia del posto auto.
     */
    public enum Type {
        /**
         * Posto auto standard.
         */
        NORMAL,

        /**
         * Posto destinato a veicoli elettrici.
         */
        ELECTRIC,

        /**
         * Posto riservato a persone con disabilità.
         */
        HANDICAPPED
    }

}
