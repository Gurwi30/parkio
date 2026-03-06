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

public class MapListener extends MouseAdapter {

    private final JXMapViewer mapViewer;
    private final ParkingLotDrawerMouseAdapter drawerMouseAdapter;
    private final ParkingLotsManager lotsManager;

    public MapListener(JXMapViewer mapViewer, ParkingLotDrawerMouseAdapter drawerMouseAdapter, ParkingLotsManager lotsManager) {
        this.mapViewer = mapViewer;
        this.drawerMouseAdapter = drawerMouseAdapter;
        this.lotsManager = lotsManager;
    }

    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint());

        getClickedParkingLot(position).ifPresentOrElse(
                _ -> mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)),
                () -> {
                    if (mapViewer.getCursor().getType() == Cursor.HAND_CURSOR) {
                        mapViewer.setCursor(Cursor.getDefaultCursor());
                    }
                }
        );

        if (e.getButton() == MouseEvent.BUTTON3) {
            drawerMouseAdapter.getInputBounds(position, Color.GREEN).onInput(bounds -> {
                bounds.ifPresent(b -> {
                    lotsManager.createParkingLot("Test", b, Color.GREEN);
                    mapViewer.repaint();
                });
            });
        }
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent e) {
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint());

        getClickedParkingLot(position).ifPresentOrElse(
                _ -> mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)),
                () -> {
                    if (mapViewer.getCursor().getType() == Cursor.HAND_CURSOR) {
                        mapViewer.setCursor(Cursor.getDefaultCursor());
                    }
                }
        );
    }

    private @NotNull Optional<ParkingLot> getClickedParkingLot(@NotNull GeoPosition clickedPosition) {
        return lotsManager.getParkingLots().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition))
                .findFirst();
    }

    private @NotNull Optional<ParkingSpace> getClickedParkingSpace(@NotNull ParkingLot parkingLot, @NotNull GeoPosition clickedPosition) {
        return parkingLot.getSpaces().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition))
                .findFirst();
    }

}
