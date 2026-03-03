package it.parkio.app.ui.component.map.listener;

import it.parkio.app.ui.ParkIOFrame;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MapListener extends MouseAdapter {

    private final JXMapViewer viewer;
    private final ParkingLotDrawerMouseAdapter drawerMouseAdapter;

    public MapListener(JXMapViewer viewer, ParkingLotDrawerMouseAdapter drawerMouseAdapter) {
        this.viewer = viewer;
        this.drawerMouseAdapter = drawerMouseAdapter;
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) return;

        GeoPosition location = viewer.convertPointToGeoPosition(e.getPoint());
        ParkIOFrame.LOGGER.debug("Clicked on: {}", location);

        drawerMouseAdapter.drawParkingLot(location).thenAccept(lotBounds -> {
            ParkIOFrame.LOGGER.info("Parking lot created with bounds: {}", lotBounds);
        });
    }

}
