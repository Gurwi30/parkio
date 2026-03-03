package it.parkio.app.ui.component.map.painter;

import it.parkio.app.model.Bounds;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;

public class MapParkingDrawerPainter implements Painter<JXMapViewer> {

    private DrawMode drawMode = DrawMode.none();
    private GeoPosition startPoint;
    private GeoPosition currentPoint;

    @Override
    public void paint(Graphics2D g, JXMapViewer mapViewer, int width, int height) {
        if (drawMode instanceof DrawMode.None || currentPoint == null) {
            return;
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Point2D p1 = mapViewer.getTileFactory().geoToPixel(startPoint, mapViewer.getZoom());
        Point2D p2 = mapViewer.getTileFactory().geoToPixel(currentPoint, mapViewer.getZoom());
        Rectangle viewport = mapViewer.getViewportBounds();

        int x = (int) (Math.min(p1.getX(), p2.getX()) - viewport.getX());
        int y = (int) (Math.min(p1.getY(), p2.getY()) - viewport.getY());
        int w = (int) Math.abs(p2.getX() - p1.getX());
        int h = (int) Math.abs(p2.getY() - p1.getY());

        switch (drawMode) {
            case DrawMode.None _ -> {
            }

            case DrawMode.WithOutBounds _ -> {
                Color previewColor = new Color(255, 200, 100, 120);
                g.setColor(previewColor);
                g.fillRect(x, y, w, h);
                g.setColor(previewColor.darker());
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10.0f, new float[]{10.0f}, 0.0f)); // Dashed
                g.drawRect(x, y, w, h);
            }

            case DrawMode.WithBounds mode -> {
                Color previewColor = new Color(100, 255, 100, 120);
                g.setColor(previewColor);
                g.fillRect(x, y, w, h);
                g.setColor(previewColor.darker());
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10.0f, new float[]{ 10.0f }, 0.0f));
                g.drawRect(x, y, w, h);

                drawRestrictionBounds(g, mapViewer, viewport, mode.bounds);
            }
        }
    }

    private void drawRestrictionBounds(@NotNull Graphics2D g, @NotNull JXMapViewer mapViewer, @NotNull Rectangle viewport, @NotNull Bounds bounds) {
        Point2D topLeft = mapViewer.getTileFactory().geoToPixel(bounds.top(), mapViewer.getZoom());
        Point2D bottomRight = mapViewer.getTileFactory().geoToPixel(bounds.bottom(), mapViewer.getZoom());

        int x = (int) (Math.min(topLeft.getX(), bottomRight.getX()) - viewport.getX());
        int y = (int) (Math.min(topLeft.getY(), bottomRight.getY()) - viewport.getY());
        int w = (int) Math.abs(bottomRight.getX() - topLeft.getX());
        int h = (int) Math.abs(bottomRight.getY() - topLeft.getY());

        g.setColor(new Color(255, 100, 100, 50));
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, w, h);
    }

    public void startDrawingLot(GeoPosition startPoint) {
        this.drawMode = DrawMode.basic(startPoint);
        this.startPoint = startPoint;
        this.currentPoint = startPoint;
    }

    public void startDrawingSpace(GeoPosition startPoint, Bounds lotBounds) {
        this.drawMode = DrawMode.withBounds(startPoint, lotBounds);
        this.startPoint = startPoint;
        this.currentPoint = startPoint;
    }

    public void updateCurrentPoint(GeoPosition currentPoint) {
        if (drawMode instanceof DrawMode.WithBounds space) {
            this.currentPoint = clampToBounds(currentPoint, space.bounds);
        } else {
            this.currentPoint = currentPoint;
        }
    }

    public Bounds stopDrawing() {
        if (drawMode instanceof DrawMode.None || startPoint == null || currentPoint == null) {
            return null;
        }

        Bounds result = new Bounds(startPoint, currentPoint);

        drawMode = DrawMode.none();
        startPoint = null;
        currentPoint = null;

        return result;
    }

    public void cancel() {
        drawMode = DrawMode.none();
        startPoint = null;
        currentPoint = null;
    }

    public boolean isDrawing() {
        return !(drawMode instanceof DrawMode.None);
    }

    private @NotNull GeoPosition clampToBounds(@NotNull GeoPosition pos, @NotNull Bounds bounds) {
        double minLat = Math.min(bounds.top().getLatitude(), bounds.bottom().getLatitude());
        double maxLat = Math.max(bounds.top().getLatitude(), bounds.bottom().getLatitude());
        double minLon = Math.min(bounds.top().getLongitude(), bounds.bottom().getLongitude());
        double maxLon = Math.max(bounds.top().getLongitude(), bounds.bottom().getLongitude());

        double lat = Math.max(minLat, Math.min(maxLat, pos.getLatitude()));
        double lon = Math.max(minLon, Math.min(maxLon, pos.getLongitude()));

        return new GeoPosition(lat, lon);
    }

    private sealed interface DrawMode {

        static DrawMode none() {
            return None.INSTANCE;
        }

        @Contract("_ -> new")
        static @NotNull DrawMode basic(GeoPosition startPoint) {
            return new WithOutBounds(startPoint);
        }

        @Contract("_, _ -> new")
        static @NotNull DrawMode withBounds(GeoPosition startPoint, Bounds bounds) {
            return new WithBounds(startPoint, bounds);
        }

        record None() implements DrawMode {
            static final None INSTANCE = new None();
        }

        record WithOutBounds(GeoPosition startPoint) implements DrawMode {

        }

        record WithBounds(GeoPosition startPoint, Bounds bounds) implements DrawMode { }

    }

}
