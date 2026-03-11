package it.parkio.app.json;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Collezione di adapter Gson usati per convertire alcuni tipi non gestiti
 * automaticamente dalla libreria JSON.
 *
 * <p>In questo caso l'obiettivo principale è gestire {@link GeoPosition},
 * che rappresenta una coordinata geografica con latitudine e longitudine.</p>
 *
 * <p>Gli adapter vengono registrati nel {@code GsonBuilder} e poi utilizzati
 * ogni volta che i dati vengono letti dal file JSON o salvati su disco.</p>
 */
public interface JsonTypeAdapters {

    /**
     * Deserializzatore di {@link GeoPosition}.
     *
     * <p>Converte un oggetto JSON del tipo:
     * <pre>{@code
     * {
     *   "latitude": 45.123,
     *   "longitude": 11.456
     * }
     * }</pre>
     * in una vera istanza Java di {@code GeoPosition}.</p>
     */
    JsonDeserializer<GeoPosition> GEO_POSITION_DESERIALIZER = (json, _, _) -> {
        // Il contenuto deve essere un oggetto JSON, non una stringa o un numero.
        if (!json.isJsonObject()) throw new JsonParseException("Expected a JsonObject");

        JsonObject obj = json.getAsJsonObject();

        // Estrae latitudine e longitudine dal JSON.
        double lat = obj.get("latitude").getAsDouble();
        double lon = obj.get("longitude").getAsDouble();

        // Crea l'oggetto geografico usato dalla mappa.
        return new GeoPosition(lat, lon);
    };

    /**
     * Serializzatore di {@link GeoPosition}.
     *
     * <p>Fa l'operazione opposta rispetto al deserializzatore:
     * prende un oggetto Java e lo trasforma in JSON
     * con i campi {@code latitude} e {@code longitude}.</p>
     */
    JsonSerializer<GeoPosition> GEO_POSITION_SERIALIZER = (src, _, _) -> {
        JsonObject jsonObject = new JsonObject();

        // Scrive le coordinate geografiche in modo esplicito e leggibile.
        jsonObject.addProperty("latitude", src.getLatitude());
        jsonObject.addProperty("longitude", src.getLongitude());

        return jsonObject;
    };

}