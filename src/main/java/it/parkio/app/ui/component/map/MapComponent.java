package it.parkio.app.ui.component.map;

import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import java.awt.*;

public class MapComponent extends JPanel {

    private static final GeoPosition FRANKFURT = new GeoPosition(50.110924, 8.682127);
    private static final int DEFAULT_ZOOM = 50;

    public MapComponent() {
        setLayout(new BorderLayout());
        add(initMap(initTileFactory()), BorderLayout.CENTER);
    }

    private @NotNull JXMapViewer initMap(DefaultTileFactory tileFactory) {
        JXMapViewer mapViewer = new JXMapViewer();

        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(DEFAULT_ZOOM);
        mapViewer.setAddressLocation(FRANKFURT);

        return mapViewer;
    }

    private @NotNull DefaultTileFactory initTileFactory() {
        TileFactoryInfo info = new OSMTileFactory();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        tileFactory.setThreadPoolSize(10);

        return tileFactory;
    }

}
