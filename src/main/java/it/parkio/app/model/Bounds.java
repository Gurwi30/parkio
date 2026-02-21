package it.parkio.app.model;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;

public record Bounds(GeoPosition top, GeoPosition bottom) {

    public static final JsonDeserializer<Bounds> DESERIALIZER = (json, _, ctx) -> {
        if (!json.isJsonObject()) throw new JsonParseException("Expected a JsonObject");

        JsonObject jsonObject = json.getAsJsonObject();

        return new Bounds(
                ctx.deserialize(jsonObject.get("top"), GeoPosition.class),
                ctx.deserialize(jsonObject.get("bottom"), GeoPosition.class)
        );
    };

    public static final JsonSerializer<Bounds> SERIALIZER = (bounds, _, ctx) -> {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("top", ctx.serialize(bounds.top));
        jsonObject.add("bottom", ctx.serialize(bounds.bottom));

        return jsonObject;
    };

    @Override
    public @NotNull String toString() {
        return String.format("Bounds { top: %s, bottom: %s }", top, bottom);
    }

}
