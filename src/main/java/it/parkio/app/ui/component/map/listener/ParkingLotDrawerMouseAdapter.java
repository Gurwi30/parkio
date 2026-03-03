package it.parkio.app.ui.component.map.listener;

import it.parkio.app.model.Bounds;
import it.parkio.app.ui.ParkIOFrame;
import it.parkio.app.ui.component.map.painter.MapParkingDrawerPainter;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CompletableFuture;

public class ParkingLotDrawerMouseAdapter extends MouseAdapter {

    private final JXMapViewer mapViewer;
    private final MapParkingDrawerPainter drawerPainter;

    private CompletableFuture<Bounds> currentDrawingFuture;

    public ParkingLotDrawerMouseAdapter(JXMapViewer mapViewer, MapParkingDrawerPainter drawerPainter) {
        this.mapViewer = mapViewer;
        this.drawerPainter = drawerPainter;
    }

    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) return;
        if (!drawerPainter.isDrawing()) return;

        GeoPosition startPoint = mapViewer.convertPointToGeoPosition(e.getPoint());

        drawerPainter.updateCurrentPoint(startPoint);
        mapViewer.repaint();

        ParkIOFrame.LOGGER.info("Drawing started at: {}", startPoint);
    }

    @Override
    public void mouseDragged(@NotNull MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == 0) return;
        if (!drawerPainter.isDrawing()) return;

        GeoPosition currentPoint = mapViewer.convertPointToGeoPosition(e.getPoint());
        drawerPainter.updateCurrentPoint(currentPoint);
        mapViewer.repaint();

        ParkIOFrame.LOGGER.info("Current point: {}", currentPoint);
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) return;
        if (!drawerPainter.isDrawing() || currentDrawingFuture == null) return;

        Bounds result = drawerPainter.stopDrawing();
        mapViewer.repaint();

        if (result != null) {
            currentDrawingFuture.complete(result);
            ParkIOFrame.LOGGER.info("Drawing completed: {}", result);
        } else {
            currentDrawingFuture.completeExceptionally(
                    new IllegalStateException("Drawing was cancelled or invalid")
            );
            ParkIOFrame.LOGGER.warn("Drawing failed or cancelled");
        }

        currentDrawingFuture = null;
    }

    public CompletableFuture<Bounds> drawParkingLot(@NotNull GeoPosition startPoint) {
        if (currentDrawingFuture != null && !currentDrawingFuture.isDone()) {
            cancelDrawing();
        }

        currentDrawingFuture = new CompletableFuture<>();
        drawerPainter.startDrawingLot(startPoint);
        mapViewer.repaint();

        ParkIOFrame.LOGGER.info("Started drawing parking lot from: {}", startPoint);

        return currentDrawingFuture;
    }

    public CompletableFuture<Bounds> drawParkingSpace(@NotNull GeoPosition startPoint, @NotNull Bounds lotBounds) {
        if (currentDrawingFuture != null && !currentDrawingFuture.isDone()) cancelDrawing();

        currentDrawingFuture = new CompletableFuture<>();
        drawerPainter.startDrawingSpace(startPoint, lotBounds);
        mapViewer.repaint();

        ParkIOFrame.LOGGER.info("Started drawing parking space from: {} within bounds: {}", startPoint, lotBounds);

        return currentDrawingFuture;
    }

    public void cancelDrawing() {
        if (currentDrawingFuture != null && !currentDrawingFuture.isDone()) {
            currentDrawingFuture.cancel(true);
            currentDrawingFuture = null;
        }

        drawerPainter.cancel();
        mapViewer.repaint();

        ParkIOFrame.LOGGER.info("Drawing cancelled");
    }

    public boolean isDrawing() {
        return currentDrawingFuture != null && !currentDrawingFuture.isDone();
    }

}