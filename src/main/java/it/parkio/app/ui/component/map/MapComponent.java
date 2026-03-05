package it.parkio.app.ui.component.map;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.Bounds;
import it.parkio.app.object.UserInputRequest;
import it.parkio.app.ui.component.map.listener.MapListener;
import it.parkio.app.ui.component.map.listener.ParkingLotDrawerMouseAdapter;
import it.parkio.app.ui.component.map.painter.MapOverlayPaintersGroup;
import it.parkio.app.ui.component.map.painter.MapParkingDrawerPainter;
import it.parkio.app.ui.component.map.painter.MapParkingPainter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.io.File;
import java.util.Optional;

public class MapComponent extends JPanel {

    private static final GeoPosition SAN_BONIFACIO_VR = new GeoPosition(45.3955, 11.2705);
    private static final int DEFAULT_ZOOM = 2;

    private final JXMapViewer mapViewer = new JXMapViewer();
    private final MapParkingDrawerPainter mapParkingDrawerPainter = new MapParkingDrawerPainter();
    private final ParkingLotDrawerMouseAdapter parkingLotDrawerMouseAdapter = new ParkingLotDrawerMouseAdapter(mapViewer, mapParkingDrawerPainter);

    private final ParkingLotsManager lotsManager;

    public MapComponent(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setLayout(new BorderLayout());
        add(initMap(initTileFactory()), BorderLayout.CENTER);
    }

    private @NotNull JXMapViewer initMap(DefaultTileFactory tileFactory) {
        MouseInputListener inputListener = new PanMouseInputListener(mapViewer);

        MapListener mapListener = new MapListener(mapViewer, parkingLotDrawerMouseAdapter, lotsManager);

        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(DEFAULT_ZOOM);
        mapViewer.setAddressLocation(SAN_BONIFACIO_VR);

        mapViewer.addMouseListener(inputListener);
        mapViewer.addMouseMotionListener(inputListener);

        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        mapViewer.addMouseListener(parkingLotDrawerMouseAdapter);
        mapViewer.addMouseMotionListener(parkingLotDrawerMouseAdapter);

        mapViewer.addMouseListener(mapListener);
        mapViewer.addMouseMotionListener(mapListener);

        mapViewer.setOverlayPainter(
                new MapOverlayPaintersGroup(new MapParkingPainter(lotsManager), mapParkingDrawerPainter)
        );

        return mapViewer;
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color, @Nullable Bounds bounds) {
        return parkingLotDrawerMouseAdapter.getInputBounds(start, color, bounds);
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color) {
        return getInputBounds(start, color, null);
    }

    private @NotNull DefaultTileFactory initTileFactory() {
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        File cacheDir = new File("cache");

        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));
        tileFactory.setThreadPoolSize(10);

        return tileFactory;
    }

}
