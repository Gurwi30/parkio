package it.parkio.app.ui.component.map.listener;

import it.parkio.app.model.Bounds;
import it.parkio.app.object.UserInputRequest;
import it.parkio.app.ui.ParkIOFrame;
import it.parkio.app.ui.component.map.painter.MapParkingDrawerPainter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class ParkingLotDrawerMouseAdapter extends MouseAdapter {

    private final JXMapViewer mapViewer;
    private final MapParkingDrawerPainter drawerPainter;

    private UserInputRequest<Optional<Bounds>> inputBoundsReq;

    public ParkingLotDrawerMouseAdapter(JXMapViewer mapViewer, MapParkingDrawerPainter drawerPainter) {
        this.mapViewer = mapViewer;
        this.drawerPainter = drawerPainter;
    }

    @Override
    public void mouseDragged(@NotNull MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == 0) return; // RIGHT MOUSE BUTTON PRESSED STATE
        if (!drawerPainter.isDrawing()) return;

        GeoPosition currentPoint = mapViewer.convertPointToGeoPosition(e.getPoint());
        drawerPainter.update(currentPoint);
        mapViewer.repaint();

        ParkIOFrame.LOGGER.debug("Updated drawerPaint curPos to {}", currentPoint);
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) return; // RIGHT MOUSE BUTTON
        if (!drawerPainter.isDrawing()) return;

        inputBoundsReq.complete(drawerPainter.stopDrawing());
        inputBoundsReq = null;

        mapViewer.setCursor(Cursor.getDefaultCursor());
        mapViewer.repaint();
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color, @Nullable Bounds bounds) {
        if (inputBoundsReq != null && !inputBoundsReq.isCompleted()) inputBoundsReq.cancel();

        inputBoundsReq = new UserInputRequest<>(this::cancelInputRequest);

        if (bounds == null) drawerPainter.startDrawing(start, color);
        else drawerPainter.startDrawing(start, color, bounds);

        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        mapViewer.repaint();

        return inputBoundsReq;
    }

    public UserInputRequest<Optional<Bounds>> getInputBounds(@NotNull GeoPosition start, @NotNull Color color) {
        return getInputBounds(start, color, null);
    }

    private void cancelInputRequest() {
        if (inputBoundsReq != null && !inputBoundsReq.isCompleted()) {
            inputBoundsReq.cancel();
            inputBoundsReq = null;
        }

        drawerPainter.stopDrawing();

        mapViewer.setCursor(Cursor.getDefaultCursor());
        mapViewer.repaint();
    }

}