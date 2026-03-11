package it.parkio.app.ui.component.map;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingSpace;
import it.parkio.app.model.ParkingSpaceStatus;
import it.parkio.app.scheduler.ParkingSpaceScheduler;
import it.parkio.app.ui.component.map.listener.MapListener;
import it.parkio.app.ui.component.map.listener.ParkingLotDrawerMouseAdapter;
import it.parkio.app.ui.component.map.painter.MapOverlayPaintersGroup;
import it.parkio.app.ui.component.map.painter.MapParkingDrawerPainter;
import it.parkio.app.ui.component.map.painter.MapParkingPainter;
import org.jetbrains.annotations.NotNull;
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
import java.time.Instant;

/**
 * Componente principale che incapsula la mappa interattiva.
 *
 * <p>Si occupa di:</p>
 * <ul>
 *     <li>inizializzare il motore tiles della mappa;</li>
 *     <li>configurare i listener di interazione;</li>
 *     <li>collegare i painter grafici;</li>
 *     <li>ripristinare lo stato corretto degli spazi al momento del caricamento.</li>
 * </ul>
 */
public class MapComponent extends JPanel {

    /**
     * Posizione iniziale mostrata all'avvio della mappa.
     */
    private static final GeoPosition SAN_BONIFACIO_VR = new GeoPosition(45.3955, 11.2705);

    /**
     * Livello di zoom iniziale.
     */
    private static final int DEFAULT_ZOOM = 2;

    /**
     * Componente mappa effettivo fornito da JXMapViewer.
     */
    private final JXMapViewer mapViewer = new JXMapViewer();

    /**
     * Painter che disegna il rettangolo temporaneo durante la creazione di aree.
     */
    private final MapParkingDrawerPainter mapParkingDrawerPainter = new MapParkingDrawerPainter();

    /**
     * Adapter che gestisce il disegno interattivo sulla mappa.
     */
    private final ParkingLotDrawerMouseAdapter parkingLotDrawerMouseAdapter = new ParkingLotDrawerMouseAdapter(mapViewer, mapParkingDrawerPainter);

    /**
     * Gestore dati dei parcheggi.
     */
    private final ParkingLotsManager lotsManager;

    /**
     * Costruisce il componente mappa e inizializza lo stato degli spazi caricati.
     *
     * @param lotsManager gestore dei parcheggi
     */
    public MapComponent(@NotNull ParkingLotsManager lotsManager) {
        this.lotsManager = lotsManager;

        setLayout(new BorderLayout());
        add(initMap(initTileFactory()), BorderLayout.CENTER);

        // Al caricamento verifica che gli stati temporali degli spazi siano ancora coerenti con l'ora attuale.
        lotsManager.getParkingLots().forEach(parkingLot -> parkingLot.getSpaces().forEach(this::fixParkingSpacesStatuse));
    }

    /**
     * Inizializza la mappa, i listener di input e i painter grafici.
     *
     * @param tileFactory factory da usare per scaricare e cacheare le tile
     * @return mappa pronta all'uso
     */
    private @NotNull JXMapViewer initMap(DefaultTileFactory tileFactory) {
        MouseInputListener inputListener = new PanMouseInputListener(mapViewer);

        MapListener mapListener = new MapListener(mapViewer, parkingLotDrawerMouseAdapter, lotsManager);

        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(DEFAULT_ZOOM);
        mapViewer.setAddressLocation(SAN_BONIFACIO_VR);

        // Listener base della libreria per trascinamento e spostamento mappa.
        mapViewer.addMouseListener(inputListener);
        mapViewer.addMouseMotionListener(inputListener);

        // Zoom tramite rotella e centratura click.
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        // Listener custom per il disegno delle aree.
        mapViewer.addMouseListener(parkingLotDrawerMouseAdapter);
        mapViewer.addMouseMotionListener(parkingLotDrawerMouseAdapter);

        // Listener custom per selezioni, tooltip e popup.
        mapViewer.addMouseListener(mapListener);
        mapViewer.addMouseMotionListener(mapListener);

        // Overlay composto: prima i parcheggi reali, poi eventuale rettangolo temporaneo.
        mapViewer.setOverlayPainter(
                new MapOverlayPaintersGroup(new MapParkingPainter(lotsManager), mapParkingDrawerPainter)
        );

        return mapViewer;
    }

    /**
     * Restituisce la mappa interna.
     *
     * @return componente JXMapViewer
     */
    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    /**
     * Inizializza la tile factory per OpenStreetMap e la relativa cache locale.
     *
     * @return tile factory configurata
     */
    private @NotNull DefaultTileFactory initTileFactory() {
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        File cacheDir = new File("cache");

        // La cache locale evita di scaricare ogni volta le stesse tile.
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));
        tileFactory.setThreadPoolSize(10);

        return tileFactory;
    }

    /**
     * Corregge lo stato di uno spazio in base all'ora attuale al momento del caricamento.
     *
     * <p>Esempi:</p>
     * <ul>
     *     <li>una riserva scaduta viene resa libera;</li>
     *     <li>una riserva già iniziata diventa occupata;</li>
     *     <li>se necessario viene programmata la transizione futura tramite scheduler.</li>
     * </ul>
     *
     * @param space spazio da verificare
     */
    private void fixParkingSpacesStatuse(@NotNull ParkingSpace space) {
        Instant now = Instant.now();

        switch (space.getStatus()) {
            case ParkingSpaceStatus.Reserved res -> {
                if (now.isAfter(res.getEnd())) {
                    space.updateStatus(ParkingSpaceStatus.free());
                } else if (now.isAfter(res.getStart())) {
                    space.updateStatus(ParkingSpaceStatus.occupied(res.getCarPlate(), res.getStart(), res.getEnd()));
                    ParkingSpaceScheduler.schedule(space, mapViewer);
                } else {
                    ParkingSpaceScheduler.schedule(space, mapViewer);
                }
            }
            case ParkingSpaceStatus.Occupied occ -> occ.getEnd().ifPresent(end -> {
                if (now.isAfter(end)) {
                    space.updateStatus(ParkingSpaceStatus.free());
                } else {
                    ParkingSpaceScheduler.schedule(space, mapViewer);
                }
            });
            default -> {}
        }
    }

}
