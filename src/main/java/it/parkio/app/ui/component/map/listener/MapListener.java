package it.parkio.app.ui.component.map.listener;

import it.parkio.app.manager.ParkingLotsManager;
import it.parkio.app.ui.ParkIOFrame;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        if (e.getButton() == MouseEvent.BUTTON3) {
            ParkIOFrame.LOGGER.debug("Button 3 pressed, requesting bounds...");

//            drawerMouseAdapter.getInputBounds(mapViewer.convertPointToGeoPosition(e.getPoint())).onInput(optionalBounds -> {
//                optionalBounds.ifPresentOrElse(
//                        bounds -> ParkIOFrame.LOGGER.debug("Got bounds: {}", bounds),
//                        () -> ParkIOFrame.LOGGER.debug("Got no bounds")
//                );
//            });
        }
    }
}
