package it.parkio.app.ui.component.map.listener;

import it.parkio.app.ParkIO;
import it.parkio.app.event.ParkingLotSelectEvent;
import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.model.ParkingLot;
import it.parkio.app.model.ParkingSpace;
import it.parkio.app.ui.component.overlay.ParkingSpaceTooltipComponent;
import it.parkio.app.ui.component.popup.ParkingLotConfiguratorPopUp;

import it.parkio.app.ui.component.popup.ParkingSpaceDetailPopUp;
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

    private final ParkingSpaceTooltipComponent parkingSpaceTooltip = new ParkingSpaceTooltipComponent();

    public MapListener(@NotNull JXMapViewer mapViewer, ParkingLotDrawerMouseAdapter drawerMouseAdapter, ParkingLotsManager lotsManager) {
        this.mapViewer = mapViewer;
        this.drawerMouseAdapter = drawerMouseAdapter;
        this.lotsManager = lotsManager;

        mapViewer.setLayout(null);
        mapViewer.add(parkingSpaceTooltip);
    }

    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint());

        if (e.getButton() == MouseEvent.BUTTON1) {
            getParkingLot(position).ifPresentOrElse(
                    parkingLot -> {
                        getParkingSpace(parkingLot, position)
                                .ifPresentOrElse(
                                        space -> new ParkingSpaceDetailPopUp(mapViewer, space),
                                        () -> ParkIO.EVENT_MANAGER.call(new ParkingLotSelectEvent(parkingLot))
                                );

                        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    },

                    () -> {
                        if (mapViewer.getCursor().getType() == Cursor.HAND_CURSOR) {
                            mapViewer.setCursor(Cursor.getDefaultCursor());
                        }
                    }
            );
        }

        if (e.getButton() == MouseEvent.BUTTON3) {
            lotsManager.getParkingLots().stream()
                    .filter(lot -> lot.getBounds().contains(position))
                    .findFirst()
                    .ifPresentOrElse(
                            lot -> drawerMouseAdapter.getInputBounds(position, lot.getColor().darker().darker(), lot.getBounds())
                                    .onInput(bounds -> bounds.ifPresent(b -> lot.addParkingSpace(b, ParkingSpace.Type.NORMAL))),

                            () -> drawerMouseAdapter.getInputBounds(position, Color.CYAN).onInput(bounds ->
                                    bounds.ifPresent(b -> new ParkingLotConfiguratorPopUp(b, lotsManager, mapViewer))
                            )
                    );
        }
    }

    @Override
    public void mouseMoved(@NotNull MouseEvent e) {
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint());

        getParkingLot(position).ifPresentOrElse(
                parkingLot -> {
                    getParkingSpace(parkingLot, position)
                            .ifPresentOrElse(
                                    space -> parkingSpaceTooltip.showTooltip(space, e.getPoint()),
                                    () -> {
                                        if (parkingSpaceTooltip.isVisible()) parkingSpaceTooltip.hideTooltip();
                                    }
                            );

                    mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                },

                () -> {
                    if (parkingSpaceTooltip.isVisible()) parkingSpaceTooltip.hideTooltip();

                    if (mapViewer.getCursor().getType() == Cursor.HAND_CURSOR) {
                        mapViewer.setCursor(Cursor.getDefaultCursor());
                    }
                }
        );
    }

    private @NotNull Optional<ParkingLot> getParkingLot(@NotNull GeoPosition clickedPosition) {
        return lotsManager.getParkingLots().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition))
                .findFirst();
    }

    private @NotNull Optional<ParkingSpace> getParkingSpace(@NotNull ParkingLot parkingLot, @NotNull GeoPosition clickedPosition) {
        return parkingLot.getSpaces().stream()
                .filter(lot -> lot.getBounds().contains(clickedPosition))
                .findFirst();
    }

}
