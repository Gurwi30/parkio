package it.parkio.app.ui.component.map.listener;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.ParkingSpace;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class MapListener extends MouseAdapter { // Listener mouse per interazioni sulla mappa

    private final JXMapViewer mapViewer; // Mappa su cui disegnare/interagire
    private final ParkingLotDrawerMouseAdapter drawerMouseAdapter; // Gestore input/creazione parcheggi
    private final ParkingLotsManager lotsManager; // Manager dei parcheggi esistenti

    public MapListener(JXMapViewer mapViewer, ParkingLotDrawerMouseAdapter drawerMouseAdapter, ParkingLotsManager lotsManager) {
        this.mapViewer = mapViewer; // inizializza mappa
        this.drawerMouseAdapter = drawerMouseAdapter; // inizializza drawer
        this.lotsManager = lotsManager; // inizializza manager parcheggi
    }

    @Override
    public void mousePressed(@NotNull MouseEvent e) { // Evento click / press mouse
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint()); // converte punto pixel -> GeoPosition

        getClickedParkingLot(position).ifPresentOrElse( // se cliccato su parcheggio
                _ -> mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)), // cambia cursore a mano
                () -> mapViewer.setCursor(Cursor.getDefaultCursor()) // altrimenti cursore default
        );

        drawerMouseAdapter.getInputBounds(position, Color.GREEN).onInput(bounds -> { // richiede input di nuova area parcheggio
            bounds.ifPresent(b -> { // se bounds validi
                lotsManager.createParkingLot("Test", b, Color.GREEN); // crea parcheggio verde di prova
                mapViewer.repaint(); // ridisegna mappa
            });
        });
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent e) { // Evento mouse move
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint()); // converte pixel -> GeoPosition

        getClickedParkingLot(position).ifPresentOrElse( // controlla se mouse sopra parcheggio
                _ -> mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)), // mano se sopra parcheggio
                () -> mapViewer.setCursor(Cursor.getDefaultCursor()) // cursore default altrimenti
        );
    }

    private @NotNull Optional<ParkingLot> getClickedParkingLot(@NotNull GeoPosition clickedPosition) { // cerca parcheggio cliccato
        return lotsManager.getParkingLots().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition)) // filtra parcheggi che contengono la posizione
                .findFirst(); // restituisce primo trovato (se esiste)
    }

    private @NotNull Optional<ParkingSpace> getClickedParkingSpace(@NotNull ParkingLot parkingLot, @NotNull GeoPosition clickedPosition) { // cerca spazio specifico nel parcheggio
        return parkingLot.getSpaces().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition)) // filtra spazi che contengono la posizione
                .findFirst(); // primo spazio trovato
    }

}