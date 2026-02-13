package it.parkio.app.ui.component.map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.TileFactoryInfo;

public class OSMTileFactory extends TileFactoryInfo {

    public OSMTileFactory() {
        super(0, 19, 19,
                256,
                true, true,
                "https://tile.openstreetmap.org",
                "x", "y", "z"
        );
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getTileUrl(int x, int y, int zoom) {
        int z = 19 - zoom;
        return this.baseURL + "/" + z + "/" + x + "/" + y + ".png";
    }

}
