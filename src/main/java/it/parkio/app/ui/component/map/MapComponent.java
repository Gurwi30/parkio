package it.parkio.app.ui.component;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import java.awt.*;

public class MapComponent extends Component {

    private final JXMapViewer map = new JXMapViewer();
    private final TileFactoryInfo info = new TileFactoryInfo(
            0, 19, 19,
            256,
            true, true,
            "https://tile.openstreetmap.org",
            "x", "y", "z"
    ) {
        @Contract(pure = true)
        @Override
        public @NotNull String getTileUrl(int x, int y, int zoom) {
            int z = 19 - zoom;
            return this.baseURL + "/" + z + "/" + x + "/" + y + ".png";
        }
    };

    private final DefaultTileFactory tileFactory = new DefaultTileFactory(info);

    @Override
    public void setup() {
        map.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(10);

        GeoPosition frankfurt = new GeoPosition(50.110924, 8.682127);

        map.setZoom(15);
        map.setAddressLocation(frankfurt);

        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
    }

}
