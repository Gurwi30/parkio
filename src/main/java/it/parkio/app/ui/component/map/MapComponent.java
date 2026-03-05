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

    private static final GeoPosition SAN_BONIFACIO_VR = new GeoPosition(45.3955, 11.2705); // Posizione iniziale della mappa
    private static final int DEFAULT_ZOOM = 2; // Zoom iniziale

    private final JXMapViewer mapViewer = new JXMapViewer(); // Componente mappa
    private final MapParkingDrawerPainter mapParkingDrawerPainter = new MapParkingDrawerPainter(); // Painter per disegnare parcheggi temporanei
    private final ParkingLotDrawerMouseAdapter parkingLotDrawerMouseAdapter = new ParkingLotDrawerMouseAdapter(mapViewer, mapParkingDrawerPainter); // Adapter per gestione input utente

    private final ParkingLotsManager lotsManager; // Manager parcheggi

    public MapComponent(ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setLayout(new BorderLayout()); // Layout del pannello
        add(initMap(initTileFactory()), BorderLayout.CENTER); // Aggiunge la mappa inizializzata
    }

    private @NotNull JXMapViewer initMap(DefaultTileFactory tileFactory) {
        MouseInputListener inputListener = new PanMouseInputListener(mapViewer); // Listener per panning

        MapListener mapListener = new MapListener(mapViewer, parkingLotDrawerMouseAdapter, lotsManager); // Listener personalizzato per click e hover

        mapViewer.setTileFactory(tileFactory); // Imposta tile factory
        mapViewer.setZoom(DEFAULT_ZOOM); // Zoom iniziale
        mapViewer.setAddressLocation(SAN_BONIFACIO_VR); // Posizione iniziale

        mapViewer.addMouseListener(inputListener); // Panning mouse listener
        mapViewer.addMouseMotionListener(inputListener);

        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer)); // Zoom con rotellina
        mapViewer.addMouseListener(new CenterMapListener(mapViewer)); // Click centrale per recentrare mappa

        mapViewer.addMouseListener(parkingLotDrawerMouseAdapter); // Gestione click/destra per disegnare parcheggi
        mapViewer.addMouseMotionListener(parkingLotDrawerMouseAdapter); // Drag mouse per disegno

        mapViewer.addMouseListener(mapListener); // Listener parcheggi click/hover
        mapViewer.addMouseMotionListener(mapListener);

        mapViewer.setOverlayPainter(
                new MapOverlayPaintersGroup(new MapParkingPainter(lotsManager), mapParkingDrawerPainter) // Gruppo di painter: parcheggi reali + disegno temporaneo
        );

        return mapViewer;
    }

    public JXMapViewer getMapViewer() {
        return mapViewer; // Espone componente mappa
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color, @Nullable Bounds bounds) {
        return parkingLotDrawerMouseAdapter.getInputBounds(start, color, bounds); // Avvia richiesta input parcheggio con eventuali limiti
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color) {
        return getInputBounds(start, color, null); // Avvia richiesta input parcheggio senza limiti
    }

    private @NotNull DefaultTileFactory initTileFactory() {
        TileFactoryInfo info = new OSMTileFactoryInfo(); // Tile provider OpenStreetMap
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        File cacheDir = new File("cache"); // Directory cache tiles

        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false)); // Imposta cache locale
        tileFactory.setThreadPoolSize(10); // Thread per caricamento tile

        return tileFactory;
    }

}