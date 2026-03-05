package it.parkio.app.ui.component.map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.TileFactoryInfo;

public class OSMTileFactoryInfo extends TileFactoryInfo { // definisce i link da dove prendere le immagini per la mappa

    public OSMTileFactoryInfo() {
        super(
                0,      // zoom minimo
                19,     // zoom massimo
                19,     // livello massimo visibile (max zoom level)
                256,    // dimensione tile in pixel
                true,   // xTiles wrap-around
                true,   // yTiles wrap-around
                "https://tile.openstreetmap.org", // URL base tiles
                "x", "y", "z" // parametri URL tile
        );
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getTileUrl(int x, int y, int zoom) {
        int z = 19 - zoom; // Inverti zoom perché OSM usa z = 0 per vista più grande
        return String.format("%s/%d/%d/%d.png", baseURL, z, x, y); // URL finale tile
    }

}