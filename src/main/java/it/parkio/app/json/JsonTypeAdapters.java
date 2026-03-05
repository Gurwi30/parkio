package it.parkio.app.json;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import org.jxmapviewer.viewer.GeoPosition;

public interface JsonTypeAdapters {

    // DESERIALIZZATORE: converte un JsonObject in GeoPosition
    JsonDeserializer<GeoPosition> GEO_POSITION_DESERIALIZER = (json, _, _) -> {
        if (!json.isJsonObject()) throw new JsonParseException("Expected a JsonObject");

        JsonObject obj = json.getAsJsonObject();

        double lat = obj.get("latitude").getAsDouble();
        double lon = obj.get("longitude").getAsDouble();

        return new GeoPosition(lat, lon);
    };

    // SERIALIZZATORE: converte un oggetto GeoPosition in JsonObject
    JsonSerializer<GeoPosition> GEO_POSITION_SERIALIZER = (src, _, _) -> {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("latitude", src.getLatitude());
        jsonObject.addProperty("longitude", src.getLongitude());

        return jsonObject;
    };

}