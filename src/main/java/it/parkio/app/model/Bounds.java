package it.parkio.app.model;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Rappresenta un'area rettangolare definita da due coordinate geografiche:
 * un punto superiore e uno inferiore.
 *
 * <p>Nel progetto viene usata per descrivere sia l'area di un parcheggio
 * sia quella di un singolo posto auto.</p>
 *
 * @param top    uno dei vertici geografici del rettangolo
 * @param bottom vertice opposto del rettangolo
 */
public record Bounds(GeoPosition top, GeoPosition bottom) {

    public static final JsonDeserializer<Bounds> DESERIALIZER = (json, _, ctx) -> {
        if (!json.isJsonObject()) throw new JsonParseException("Expected a JsonObject");

        JsonObject jsonObject = json.getAsJsonObject();

        return new Bounds(
                ctx.deserialize(jsonObject.get("top"), GeoPosition.class),
                ctx.deserialize(jsonObject.get("bottom"), GeoPosition.class)
        );
    };

    /**
     * Serializzatore JSON di {@link Bounds}.
     *
     * <p>Converte i due vertici geografici in un oggetto JSON
     * facilmente salvabile su file.</p>
     */
    public static final JsonSerializer<Bounds> SERIALIZER = (bounds, _, ctx) -> {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("top", ctx.serialize(bounds.top));
        jsonObject.add("bottom", ctx.serialize(bounds.bottom));

        return jsonObject;
    };

    /**
     * Verifica se una posizione geografica cade all'interno di questi bounds.
     *
     * <p>Il metodo calcola i valori minimi e massimi di latitudine e longitudine,
     * così funziona correttamente indipendentemente dall'ordine esatto
     * con cui i due punti sono stati salvati.</p>
     *
     * @param position posizione da controllare
     * @return {@code true} se il punto è interno al rettangolo, altrimenti {@code false}
     */
    public boolean contains(@NotNull GeoPosition position) {

        double minLat = Math.min(top.getLatitude(), bottom.getLatitude());
        double maxLat = Math.max(top.getLatitude(), bottom.getLatitude());

        double minLon = Math.min(top.getLongitude(), bottom.getLongitude());
        double maxLon = Math.max(top.getLongitude(), bottom.getLongitude());

        double lat = position.getLatitude();
        double lon = position.getLongitude();

        return lat >= minLat && lat <= maxLat
                && lon >= minLon && lon <= maxLon;
    }

    /**
     * Restituisce una rappresentazione testuale utile per debug e logging.
     */
    @Override
    public @NotNull String toString() {
        return String.format("Bounds { top: %s, bottom: %s }", top, bottom);
    }

}
