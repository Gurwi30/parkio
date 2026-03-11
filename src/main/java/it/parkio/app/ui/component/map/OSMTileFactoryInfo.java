package it.parkio.app.ui.component.map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * Configurazione della sorgente tile per OpenStreetMap.
 *
 * <p>Questa classe dice a JXMapViewer come costruire gli URL
 * da cui scaricare le immagini della mappa.</p>
 */
public class OSMTileFactoryInfo extends TileFactoryInfo {

    /**
     * Inizializza i parametri base del provider OpenStreetMap.
     */
    public OSMTileFactoryInfo() {
        super(
                0,
                19,
                19,
                256,
                true,
                true,
                "https://tile.openstreetmap.org",
                "x", "y", "z"
        );
    }

    /**
     * Costruisce l'URL della tile richiesta.
     *
     * <p>JXMapViewer usa una convenzione di zoom diversa rispetto a OSM,
     * quindi qui viene applicata l'inversione necessaria.</p>
     *
     * @param x    coordinata x della tile
     * @param y    coordinata y della tile
     * @param zoom livello di zoom interno della libreria
     * @return URL completo della tile PNG
     */
    @Contract(pure = true)
    @Override
    public @NotNull String getTileUrl(int x, int y, int zoom) {
        int z = 19 - zoom;
        return String.format("%s/%d/%d/%d.png", baseURL, z, x, y);
    }

}